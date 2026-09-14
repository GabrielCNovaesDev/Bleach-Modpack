package com.bleachmod.server.travel;

import com.bleachmod.common.data.TravelData;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.NbtIo;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Runs before levels are opened. Never merges or overwrites an existing dimension. */
public final class SoulSocietyMapInstaller {
    public static final String MANIFEST = "bleach-map.json";
    public record InstalledMap(int version, int dataVersion, TravelData.Location arrival, String sha256, long bytes, int files) {}
    private SoulSocietyMapInstaller() {}

    public static Optional<InstalledMap> installed(Path target) throws IOException {
        Path manifest = target.resolve(MANIFEST);
        if (!Files.exists(manifest)) return Optional.empty();
        rejectLinks(manifest);
        if (Files.size(manifest)>16_384) throw new IOException("Installed map manifest too large");
        try (var reader = Files.newBufferedReader(manifest)) {
            var map = new GsonBuilder().create().fromJson(reader, InstalledMap.class);
            if (map == null || map.version != 1 || map.arrival == null || map.files < 1 ||
                !map.arrival.dimension().equals(SoulSociety.LEVEL.location().toString())) throw new IOException("Invalid installed map manifest");
            if (!Files.isDirectory(target.resolve("region"))) throw new IOException("Installed map is missing region data");
            return Optional.of(map);
        } catch (RuntimeException e) { throw new IOException("Invalid installed map manifest", e); }
    }

    public static Optional<InstalledMap> install(Path source, Path target) throws IOException {
        source = source.toAbsolutePath().normalize(); target = target.toAbsolutePath().normalize();
        Path backup = target.resolveSibling(".soul-society-before-import");
        rejectLinks(target); rejectLinks(backup);
        // Recover a process interruption between saving the old metadata and publishing staging.
        if (Files.exists(backup) && !Files.exists(target)) Files.move(backup, target);
        var existing = installed(target);
        if (existing.isPresent()) return existing;
        if (!Files.exists(source.resolve("level.dat"))) return Optional.empty();
        if (source.startsWith(target) || target.startsWith(source)) throw new IOException("Source and destination overlap");
        rejectLinks(source);
        rejectLinks(source.resolve("level.dat"));
        rejectLinks(source.resolve("session.lock"));
        // Minecraft holds this lock while the source world is open. Never copy a live save.
        try (var channel = FileChannel.open(source.resolve("session.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = channel.tryLock()) {
            if (lock == null) throw new IOException("Close the source world before importing Soul Society");
            return Optional.of(copy(source, target));
        } catch (java.nio.channels.OverlappingFileLockException e) { throw new IOException("Source world is open", e); }
    }

    private static InstalledMap copy(Path source, Path target) throws IOException {
        Path metadata = source.resolve("level.dat");
        if (Files.size(metadata) > 8 * 1024 * 1024) throw new IOException("level.dat exceeds 8 MiB");
        var data = NbtIo.readCompressed(metadata.toFile()).getCompound("Data");
        if (data.getInt("DataVersion") != 3465) throw new IOException("Map must be saved in Minecraft Java 1.20.1 (DataVersion 3465)");
        for (String key : List.of("SpawnX", "SpawnY", "SpawnZ"))
            if (!data.contains(key, 99)) throw new IOException("Map has no valid world spawn");
        TravelData.Location arrival;
        try { arrival = new TravelData.Location(SoulSociety.LEVEL.location().toString(), data.getInt("SpawnX") + 0.5,
            data.getInt("SpawnY"), data.getInt("SpawnZ") + 0.5, data.getFloat("SpawnAngle"), 0); }
        catch (IllegalArgumentException e) { throw new IOException("Invalid map spawn", e); }
        var files = collect(source);
        if (files.stream().noneMatch(p -> p.getParent().getFileName().toString().equals("region") && p.toString().endsWith(".mca")))
            throw new IOException("Map has no Overworld region files");
        requireUnpopulated(target);
        Files.createDirectories(target.getParent());
        Path staging = Files.createTempDirectory(target.getParent(), ".soul-society-import-");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long bytes = 0;
            for (Path path : files) {
                Path relative = source.relativize(path);
                digest.update(relative.toString().replace('\\', '/').getBytes(java.nio.charset.StandardCharsets.UTF_8));
                Path output = staging.resolve(relative);
                Files.createDirectories(output.getParent());
                try (var input = new java.security.DigestInputStream(Files.newInputStream(path), digest)) {
                    bytes += Files.copy(input, output);
                }
            }
            // A dimension without chunks still saves raids/capability metadata on normal shutdown.
            // Preserve it so a player can install the map after already playing this Bleach save.
            Path oldData = target.resolve("data");
            if (Files.isDirectory(oldData)) {
                Files.createDirectories(staging.resolve("data"));
                try (var paths = Files.list(oldData)) {
                    for (Path path : paths.toList()) Files.copy(path, staging.resolve("data").resolve(path.getFileName()));
                }
            }
            var map = new InstalledMap(1, 3465, arrival, HexFormat.of().formatHex(digest.digest()), bytes, files.size());
            Files.writeString(staging.resolve(MANIFEST), new GsonBuilder().setPrettyPrinting().create().toJson(map));
            requireUnpopulated(target);
            Path backup = target.resolveSibling(".soul-society-before-import");
            if (Files.exists(backup)) throw new IOException("Previous import backup requires inspection: " + backup);
            if (Files.exists(target)) Files.move(target, backup);
            try {
                try { Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException e) { Files.move(staging, target); }
            } catch (IOException e) {
                if (!Files.exists(target) && Files.exists(backup)) Files.move(backup, target);
                throw e;
            }
            return map;
        } catch (NoSuchAlgorithmException e) { throw new IOException(e); }
        finally {
            if (Files.exists(staging)) {
                try (var paths = Files.walk(staging)) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
                }
            }
        }
    }

    private static List<Path> collect(Path source) throws IOException {
        List<Path> result = new ArrayList<>(); long bytes = 0;
        for (String name : List.of("region", "entities", "poi")) {
            Path dir = source.resolve(name);
            if (!Files.exists(dir)) continue;
            rejectLinks(dir);
            try (var paths = Files.list(dir)) {
                for (Path path : paths.sorted().toList()) {
                    rejectLinks(path);
                    if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) ||
                        !path.getFileName().toString().matches("(?:r\\.-?\\d+\\.-?\\d+\\.mca|c\\.-?\\d+\\.-?\\d+\\.mcc)"))
                        throw new IOException("Unexpected map file: " + path.getFileName());
                    // Minecraft leaves zero-byte region files when opening an empty entity/POI region.
                    // They contain no chunks; skipping them preserves the world's meaning.
                    if (path.toString().endsWith(".mca") && Files.size(path)==0) continue;
                    bytes += Files.size(path); result.add(path);
                    if(path.toString().endsWith(".mca") && Files.size(path)<8192) throw new IOException("Truncated region header: "+path.getFileName());
                    if (result.size() > 100_000 || bytes > 32L * 1024 * 1024 * 1024) throw new IOException("Map exceeds import limit (100000 files / 32 GiB)");
                }
            }
        }
        return result;
    }

    private static void rejectLinks(Path path) throws IOException {
        for (Path p = path.toAbsolutePath(); p != null; p = p.getParent())
            if (Files.isSymbolicLink(p) || (Files.exists(p) && Files.readAttributes(p, java.nio.file.attribute.BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isOther()))
                throw new IOException("Linked paths are not supported: " + p);
    }
    private static void requireUnpopulated(Path target) throws IOException {
        rejectLinks(target);
        if (!Files.exists(target)) return;
        try (var paths = Files.walk(target)) {
            for (Path p : paths.toList()) {
                rejectLinks(p);
                if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) continue;
                if (!p.getParent().equals(target.resolve("data")) || !p.toString().endsWith(".dat") || Files.size(p)>16*1024*1024)
                    throw new IOException("Soul Society already has chunks or unknown files; refusing to overwrite " + p);
            }
        }
    }
}
