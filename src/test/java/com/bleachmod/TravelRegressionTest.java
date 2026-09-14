package com.bleachmod;

import com.bleachmod.common.data.*;
import com.bleachmod.common.network.TravelPackets;
import com.bleachmod.server.travel.TravelConfig;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import java.util.Objects;

public final class TravelRegressionTest {
    private static int count;
    public static void run() {
        test("legacy save starts without a return",()->{
            PlayerData data=new PlayerData(); CompoundTag tag=new CompoundTag();tag.putInt("schemaVersion",3);data.load(tag);
            eq(null,data.getTravel().getReturnLocation());eq(0,data.getTravel().remaining(0));
        });
        test("travel state survives full sync and clone copy",()->{
            PlayerData data=new PlayerData(); var location=new TravelData.Location("minecraft:overworld",13.5,80,-32.5,90,0);
            data.getTravel().setReturnLocation(location);data.getTravel().setCooldown(1000,100);
            PlayerData copy=new PlayerData();copy.copyFrom(data);
            eq(location,copy.getTravel().getReturnLocation());eq(60,copy.getTravel().remaining(1040));
            copy.resetTransientState();eq(60,copy.getTravel().remaining(1040));
            copy.load(new CompoundTag());eq(location,copy.getTravel().getReturnLocation());
        });
        test("invalid return cannot turn into an origin-zero teleport",()->{
            TravelData data=new TravelData();CompoundTag tag=new CompoundTag();tag.put("returnLocation",new CompoundTag());data.load(tag);
            eq(null,data.getReturnLocation());
            rejects(()->new TravelData.Location("INVALID ID",0,64,0,0,0));
            rejects(()->new TravelData.Location("minecraft:overworld",Double.NaN,64,0,0,0));
            rejects(()->new TravelData.Location("minecraft:overworld",0,400,0,0,0));
        });
        test("corrupt cooldown is bounded and expires",()->{
            TravelData data=new TravelData();CompoundTag tag=new CompoundTag();tag.putLong("nextTravelAt",Long.MAX_VALUE);data.load(tag);
            eq(72000,data.remaining(10));eq(0,data.remaining(72010));
        });
        test("travel config rejects invalid costs and cooldowns",()->{
            rejects(()->new TravelConfig(true,Float.NaN,100,null));
            rejects(()->new TravelConfig(true,-1,100,null));
            rejects(()->new TravelConfig(true,20,-1,null));
            rejects(()->new TravelConfig(true,20,100,new TravelData.Location("minecraft:overworld",0,64,0,0,0)));
        });
        test("travel packet roundtrip has only request id target and revision",()->{
            var buf=new FriendlyByteBuf(Unpooled.buffer());
            try {
                var request=new TravelPackets.Request(24,"bleachmod:return",7);TravelPackets.Request.encode(request,buf);
                eq(request,TravelPackets.Request.decode(buf));eq(0,buf.readableBytes());
            } finally { buf.release(); }
        });
        test("travel decoder rejects oversized ids",()->{
            var buf=new FriendlyByteBuf(Unpooled.buffer());
            try {
                buf.writeInt(1);buf.writeUtf("a".repeat(129));buf.writeInt(2);
                try { TravelPackets.Request.decode(buf);throw new AssertionError("Oversized id accepted"); }
                catch(io.netty.handler.codec.DecoderException expected) {}
            } finally { buf.release(); }
        });
        System.out.println("PASS: "+count+" travel regression scenarios");
    }
    private static void test(String name,Runnable task) { task.run();count++;System.out.println("PASS "+name); }
    private static void eq(Object a,Object b) { if(!Objects.equals(a,b))throw new AssertionError(a+" != "+b); }
    private static void rejects(Runnable task) { try { task.run(); }catch(IllegalArgumentException e){return;}throw new AssertionError("Expected rejection"); }
}
