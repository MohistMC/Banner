package com.mohistmc.banner.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mohistmc.banner.BannerMCStart;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class BannerMixinPlugin implements IMixinConfigPlugin {

    private final Map<String, Map.Entry<List<FieldNode>, List<MethodNode>>> accessTransformer =
            ImmutableMap.<String, Map.Entry<List<FieldNode>, List<MethodNode>>>builder()
                    .put("net.minecraft.world.item.BoneMealItem",
                            Maps.immutableEntry(
                                    ImmutableList.of(),
                                    ImmutableList.of(
                                            new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "applyBonemeal", "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", null, null)
                                    )
                            )
                    ).put("net.minecraft.world.level.block.DispenserBlock",
                            Maps.immutableEntry(
                                    ImmutableList.of(
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.T_BOOLEAN, "eventFired", "Z", null, null)
                                    ),
                                    ImmutableList.of()
                            )
                    ).put("net.minecraft.world.item.SignItem",
                            Maps.immutableEntry(
                                    ImmutableList.of(
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.T_BOOLEAN, "openSign", "Lnet/minecraft/core/BlockPos;", null, null)
                                    ),
                                    ImmutableList.of()
                            )
                    ).put("net.minecraft.network.chat.ChatType",
                            Maps.immutableEntry(
                                    ImmutableList.of(
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "RAW", "Lnet/minecraft/resources/ResourceKey;", null, null)
                                    ),
                                    ImmutableList.of()
                            )
                    )
                    .put("net.minecraft.world.level.block.ComposterBlock",
                            Maps.immutableEntry(
                                    ImmutableList.of(),
                                    ImmutableList.of(
                                            new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "addItem", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;D)Lnet/minecraft/world/level/block/state/BlockState;", null, null)
                                    )
                            )
                    )
                    .put("net.minecraft.server.commands.ReloadCommand",
                            Maps.immutableEntry(
                                    ImmutableList.of(),
                                    ImmutableList.of(
                                            new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "reload", "(Lnet/minecraft/server/MinecraftServer;)V", null, null)
                                    )
                            )
                    )
                    .put("net.minecraft.server.MinecraftServer",
                            Maps.immutableEntry(
                                    ImmutableList.of(
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "currentTick", "I", null, null)
                                    ),
                                    ImmutableList.of(
                                            new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getServer", "()Lnet/minecraft/server/MinecraftServer;", null, null)
                                    )
                            ))
                    .put("net.minecraft.world.item.LeadItem",
                            Maps.immutableEntry(
                                    ImmutableList.of(),
                                    ImmutableList.of(
                                            new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "bindPlayerMobs", "Lnet/minecraft/world/InteractionResult;", "Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/InteractionHand;", null)
                                    )
                            ))
                    .put("net.minecraft.server.level.TicketType",
                            Maps.immutableEntry(
                                    ImmutableList.of(
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "PLUGIN",
                                                    "Lnet/minecraft/server/level/TicketType;", null, null),
                                            new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "PLUGIN_TICKET",
                                                    "Lnet/minecraft/server/level/TicketType;", null, null)
                                    ),
                                    ImmutableList.of()
                            ))
                    .build();

    @Override
    public void onLoad(String mixinPackage) {
        try {
            BannerMCStart.run();
        } catch (Exception ex) {
            BannerMCStart.LOGGER.error("Failed to load BannerServer..., caused by " + ex.getCause());
            throw new RuntimeException(ex);
        }
    }

    private final Set<String> modifyConstructor = ImmutableSet.<String>builder()
            .add("net.minecraft.class_1937")
            .add("net.minecraft.class_3218")
            .add("net.minecraft.class_1277")
            .add("net.minecraft.class_3962")
            .add("net.minecraft.class_3962$class_3925")
            .add("net.minecraft.class_1702")
            .add("net.minecraft.class_8566")
            .add("net.minecraft.class_1730")
            .add("net.minecraft.class_1914")
            .add("net.minecraft.class_3916")
            .add("net.minecraft.class_3231")
            .add("net.minecraft.class_2815")
            .add("net.minecraft.class_5251")
            .add("net.minecraft.class_2170")
            .add("net.minecraft.class_32$class_5143")
            .add("net.minecraft.class_7439")
            .add("net.minecraft.class_2637")
            .add("net.minecraft.class_3320")
            .add("net.minecraft.class_2347")
            .add("net.minecraft.class_2897")
            .add("net.minecraft.class_3350")
            .add("net.minecraft.class_2826")
            .build();

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("com.mohistmc.banner.mixin.world.entity.MixinMob$PaperSpawnAffect")) {
            return !FabricLoader.getInstance().isModLoaded("vmp");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.world.level.spawner.MixinNaturalSpawner")) {
            return !FabricLoader.getInstance().isModLoaded("carpet-tis-addition")
                    && !FabricLoader.getInstance().isModLoaded("carpet");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.network.protocol.MixinPacketUtils")) {
            return !FabricLoader.getInstance().isModLoaded("cobblemon");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.world.item.MixinChorusFruitItem")) {
            return !FabricLoader.getInstance().isModLoaded("openpartiesandclaims");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.world.level.MixinClipContext")) {
            return !FabricLoader.getInstance().isModLoaded("create") && !FabricLoader.getInstance().isModLoaded("porting_lib");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        Map.Entry<List<FieldNode>, List<MethodNode>> entry = accessTransformer.get(targetClassName);
        if (entry != null) {
            List<FieldNode> fields = entry.getKey();
            for (FieldNode fieldNode : targetClass.fields) {
                tryTransform(fields, fieldNode);
            }
            List<MethodNode> methods = entry.getValue();
            for (MethodNode methodNode : targetClass.methods) {
                tryTransform(methods, methodNode);
            }
        }
        modifyConstructor(targetClassName, targetClass);
    }

    private void modifyConstructor(String targetClassName, ClassNode classNode) {
        if (modifyConstructor.contains(targetClassName)) {
            Set<String> presentCtor = new HashSet<>();
            Set<String> overrideCtor = new HashSet<>();
            for (MethodNode method : classNode.methods) {
                if (method.name.equals("<init>")) {
                    presentCtor.add(method.desc);
                }
                if (method.name.equals("banner$constructor$override")) {
                    overrideCtor.add(method.desc);
                }
            }
            ListIterator<MethodNode> iterator = classNode.methods.listIterator();
            while (iterator.hasNext()) {
                MethodNode methodNode = iterator.next();
                if (methodNode.name.equals("banner$constructor")) {
                    String desc = methodNode.desc;
                    if (presentCtor.contains(desc)) {
                        iterator.remove();
                    } else {
                        methodNode.name = "<init>";
                        presentCtor.add(methodNode.desc);
                        remapCtor(classNode, methodNode);
                    }
                }
                if (methodNode.name.equals("banner$constructor$super")) {
                    iterator.remove();
                }
                if (methodNode.name.equals("<init>") && overrideCtor.contains(methodNode.desc)) {
                    iterator.remove();
                } else if (methodNode.name.equals("banner$constructor$override")) {
                    methodNode.name = "<init>";
                    remapCtor(classNode, methodNode);
                }
            }
        }
    }

    private void remapCtor(ClassNode classNode, MethodNode methodNode) {
        boolean initialized = false;
        for (AbstractInsnNode node : methodNode.instructions) {
            if (node instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.name.equals("banner$constructor")) {
                    if (initialized) {
                        throw new ClassFormatError("Duplicate constructor call");
                    } else {
                        methodInsnNode.setOpcode(Opcodes.INVOKESPECIAL);
                        methodInsnNode.name = "<init>";
                        initialized = true;
                    }
                }
                if (methodInsnNode.name.equals("banner$constructor$super")) {
                    if (initialized) {
                        throw new ClassFormatError("Duplicate constructor call");
                    } else {
                        methodInsnNode.setOpcode(Opcodes.INVOKESPECIAL);
                        methodInsnNode.owner = classNode.superName;
                        methodInsnNode.name = "<init>";
                        initialized = true;
                    }
                }
            }
        }
        if (!initialized) {
            if (classNode.superName.equals("java/lang/Object")) {
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
                methodNode.instructions.insert(insnList);
            } else {
                throw new ClassFormatError("No super constructor call present: " + classNode.name);
            }
        }
    }

    private void tryTransform(List<FieldNode> fields, FieldNode fieldNode) {
        for (FieldNode field : fields) {
            if (Objects.equals(fieldNode.name, field.name)
                    && Objects.equals(fieldNode.desc, field.desc)) {
                fieldNode.access = field.access;
            }
        }
    }

    private void tryTransform(List<MethodNode> methods, MethodNode methodNode) {
        for (MethodNode method : methods) {
            if (Objects.equals(methodNode.name, method.name)
                    && Objects.equals(methodNode.desc, method.desc)) {
                methodNode.access = method.access;
            }
        }
    }
}
