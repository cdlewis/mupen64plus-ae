LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
SRCDIR := ../../src

LOCAL_MODULE := core
LOCAL_SHARED_LIBRARIES := SDL2
LOCAL_STATIC_LIBRARIES := png
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES :=         \
    $(LOCAL_PATH)/$(SRCDIR) \
    $(PNG_INCLUDES)         \
    $(SDL_INCLUDES)         \
    $(SYSROOT)/usr/include/ \
#   ^ Workaround for some reason 4.6 gcc doesnt include the usr/include directory

LOCAL_SRC_FILES :=                              \
    $(SRCDIR)/api/callbacks.c                   \
    $(SRCDIR)/api/common.c                      \
    $(SRCDIR)/api/config.c                      \
    $(SRCDIR)/api/debugger.c                    \
    $(SRCDIR)/api/frontend.c                    \
    $(SRCDIR)/api/vidext.c                      \
    $(SRCDIR)/main/cheat.c                      \
    $(SRCDIR)/main/eventloop.c                  \
    $(SRCDIR)/main/main.c                       \
    $(SRCDIR)/main/md5.c                        \
    $(SRCDIR)/main/rom.c                        \
    $(SRCDIR)/main/savestates.c                 \
    $(SRCDIR)/main/sdl_key_converter.c          \
    $(SRCDIR)/main/util.c                       \
    $(SRCDIR)/main/zip/ioapi.c                  \
    $(SRCDIR)/main/zip/unzip.c                  \
    $(SRCDIR)/main/zip/zip.c                    \
    $(SRCDIR)/memory/dma.c                      \
    $(SRCDIR)/memory/flashram.c                 \
    $(SRCDIR)/memory/memory.c                   \
    $(SRCDIR)/memory/n64_cic_nus_6105.c         \
    $(SRCDIR)/memory/pif.c                      \
    $(SRCDIR)/memory/tlb.c                      \
    $(SRCDIR)/osal/dynamiclib_unix.c            \
    $(SRCDIR)/osal/files_unix.c                 \
    $(SRCDIR)/osd/screenshot.cpp                \
    $(SRCDIR)/plugin/dummy_audio.c              \
    $(SRCDIR)/plugin/dummy_input.c              \
    $(SRCDIR)/plugin/dummy_rsp.c                \
    $(SRCDIR)/plugin/dummy_video.c              \
    $(SRCDIR)/plugin/plugin.c                   \
    $(SRCDIR)/r4300/exception.c                 \
    $(SRCDIR)/r4300/interupt.c                  \
    $(SRCDIR)/r4300/profile.c                   \
    $(SRCDIR)/r4300/pure_interp.c               \
    $(SRCDIR)/r4300/r4300.c                     \
    $(SRCDIR)/r4300/recomp.c                    \
    $(SRCDIR)/r4300/reset.c                     \
    #$(SRCDIR)/debugger/dbg_breakpoints.c        \
    #$(SRCDIR)/debugger/dbg_decoder.c            \
    #$(SRCDIR)/debugger/dbg_memory.c             \
    #$(SRCDIR)/debugger/debugger.c               \

LOCAL_CFLAGS :=         \
    $(COMMON_CFLAGS)    \
    -DANDROID           \
    -DIOAPI_NO_64       \
    -DNOCRYPT           \
    -DNOUNCRYPT         \

LOCAL_LDFLAGS :=                                                    \
    -Wl,-Bsymbolic                                                  \
    -Wl,-export-dynamic                                             \
    -Wl,-version-script,$(LOCAL_PATH)/$(SRCDIR)/api/api_export.ver  \

LOCAL_LDLIBS := -lz

ifeq ($(TARGET_ARCH_ABI), armeabi-v7a)
    # Use for ARM7a:
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/empty_dynarec.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/new_dynarec/new_dynarec.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/new_dynarec/linkage_arm.S
    LOCAL_CFLAGS += -DDYNAREC
    LOCAL_CFLAGS += -DNEW_DYNAREC=3
    LOCAL_CFLAGS += -mfloat-abi=softfp
    LOCAL_CFLAGS += -mfpu=vfp

else ifeq ($(TARGET_ARCH_ABI), armeabi)
    # Use for pre-ARM7a:
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/empty_dynarec.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/new_dynarec/new_dynarec.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/new_dynarec/linkage_arm.S
    LOCAL_CFLAGS += -DARMv5_ONLY
    LOCAL_CFLAGS += -DDYNAREC
    LOCAL_CFLAGS += -DNEW_DYNAREC=3

else ifeq ($(TARGET_ARCH_ABI), x86)
    # Use for x86:
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/assemble.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gbc.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop0.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop1.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop1_d.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop1_l.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop1_s.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gcop1_w.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gr4300.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gregimm.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gspecial.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/gtlb.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/regcache.c
    LOCAL_SRC_FILES += $(SRCDIR)/r4300/x86/rjump.c
    LOCAL_CFLAGS += -DDYNAREC

else ifeq ($(TARGET_ARCH_ABI), mips)
    # Use for MIPS:
    #TODO: Possible to port dynarec from Daedalus? 

else
    # Any other architectures that Android could be running on?

endif

include $(BUILD_SHARED_LIBRARY)
