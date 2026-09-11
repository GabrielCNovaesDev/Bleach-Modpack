package com.bleachmod.server.commands;
import com.bleachmod.Reference;
import com.bleachmod.common.ProgressionService;
import com.bleachmod.common.RegistryReload;
import com.bleachmod.common.data.*;
import com.bleachmod.common.network.SyncHelper;
import com.bleachmod.common.quest.QuestRegistry;
import com.bleachmod.common.evolution.FormRegistry;
import com.bleachmod.init.ModItems;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.commands.Commands.*;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public final class BleachCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("bleachreload").requires(s->s.hasPermission(2))
            .then(literal("quests").executes(ctx->{
                try {
                    RegistryReload.reload(ctx.getSource().getServer());
                    for(ServerPlayer player:ctx.getSource().getServer().getPlayerList().getPlayers()) {
                        PlayerCapability.get(player).ifPresent(data->ProgressionService.sync(player,data));
                        SyncHelper.questRegistry(player);
                    }
                    ctx.getSource().sendSuccess(()->Component.translatable("command.bleachmod.reload.quests"),true);
                    return 1;
                } catch(Exception e) {
                    com.bleachmod.BleachMod.LOGGER.error("Bleach reload rejected",e);
                    ctx.getSource().sendFailure(Component.literal("Bleach: "+e.getMessage()));
                    return 0;
                }
            })));
        var root=literal("bleachdev").requires(s->s.hasPermission(2));
        root.then(literal("points").then(literal("add").then(argument("player",EntityArgument.player())
            .then(argument("amount",IntegerArgumentType.integer(1,1000000)).executes(ctx->mutate(ctx,(p,d)->d.getResources().addTrainingPoints(IntegerArgumentType.getInteger(ctx,"amount"))))))));
        root.then(literal("skill").then(literal("set").then(argument("player",EntityArgument.player())
            .then(literal("zanpakuto").then(argument("level",IntegerArgumentType.integer(0,2)).executes(ctx->mutate(ctx,(p,d)->{
                int level=IntegerArgumentType.getInteger(ctx,"level");
                d.getSkills().get("zanpakuto").setLevel(level);
                if(level>=1)d.getCharacter().unlockForm("shikai");
                if(level>=2)d.getCharacter().unlockForm("bankai");
            })))))));
        root.then(literal("mastery").then(literal("set").then(argument("player",EntityArgument.player())
            .then(literal("zanpakuto").then(argument("form",StringArgumentType.word())
                .suggests((ctx,b)->SharedSuggestionProvider.suggest(java.util.List.of("sealed","shikai","bankai"),b))
                .then(argument("value",DoubleArgumentType.doubleArg(0,100)).executes(ctx->mutate(ctx,(p,d)->{
                    String form=StringArgumentType.getString(ctx,"form");
                    var definition=FormRegistry.getForm("shinigami","zanpakuto",form);
                    if(definition==null) throw new IllegalArgumentException("Unknown form");
                    double value=DoubleArgumentType.getDouble(ctx,"value");
                    if(value>definition.getMaxMastery()) throw new IllegalArgumentException("Mastery exceeds configured maximum");
                    d.getCharacter().setMastery("zanpakuto",form,value);
                }))))))));
        root.then(literal("reiatsu").then(literal("fill").then(argument("player",EntityArgument.player())
            .executes(ctx->mutate(ctx,(p,d)->d.getResources().setCurrentReiatsu(d.getResources().getMaxReiatsu()))))));
        root.then(literal("asauchi").then(literal("give").then(argument("player",EntityArgument.player())
            .executes(ctx->mutate(ctx,(p,d)->{
                ItemStack stack=new ItemStack(ModItems.ASAUCHI.get());
                if(!p.getInventory().add(stack)) p.drop(stack,false);
            })))));
        root.then(literal("inspect").then(argument("player",EntityArgument.player()).executes(ctx->{
            ServerPlayer p=EntityArgument.getPlayer(ctx,"player");
            PlayerData d=SyncHelper.require(p);
            ctx.getSource().sendSuccess(()->Component.literal(p.getScoreboardName()+": "+d.getCharacter().getRace()
                +" | "+d.getCharacter().getActiveForm()+" -> "+d.getCharacter().getSelectedForm()
                +" | reiatsu="+d.getResources().getCurrentReiatsu()+"/"+d.getResources().getMaxReiatsu()
                +" | BP="+Math.round(d.getBattlePower())
                +" | points="+d.getResources().getTrainingPoints()+" | zanpakuto="+d.getSkills().getLevel("zanpakuto")
                +" | mastery="+d.getCharacter().getMastery("zanpakuto","shikai")+"/"+d.getCharacter().getMastery("zanpakuto","bankai")),false);
            return 1;
        })));
        event.getDispatcher().register(root);
    }
    private interface Mutation { void apply(ServerPlayer player,PlayerData data); }
    private static int mutate(CommandContext<CommandSourceStack> ctx,Mutation action) throws CommandSyntaxException {
        ServerPlayer player=EntityArgument.getPlayer(ctx,"player");
        PlayerData data=SyncHelper.require(player);
        if(!data.getStatus().hasCreatedCharacter()) throw new SimpleCommandExceptionType(Component.literal("Confirm the character first")).create();
        try { action.apply(player,data); }
        catch(IllegalArgumentException e) { throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create(); }
        ProgressionService.sync(player,data);
        ctx.getSource().sendSuccess(()->Component.literal("Bleach: "+player.getScoreboardName()+" updated"),true);
        return 1;
    }
}
