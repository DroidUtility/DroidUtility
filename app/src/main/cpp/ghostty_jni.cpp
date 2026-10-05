#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include <ghostty/vt.h>

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeCreate(JNIEnv *env, jobject thiz, jint cols, jint rows) {
    GhosttyTerminal term;
    GhosttyTerminalOptions opts = GHOSTTY_INIT_SIZED(GhosttyTerminalOptions);
    opts.cols = (uint16_t)cols;
    opts.rows = (uint16_t)rows;
    opts.max_scrollback = 1000;
    ghostty_terminal_new(NULL, &term, opts);
    return reinterpret_cast<jlong>(term);
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeFree(JNIEnv *env, jobject thiz, jlong handle) {
    auto term = reinterpret_cast<GhosttyTerminal>(handle);
    ghostty_terminal_free(term);
}

JNIEXPORT jbyteArray JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeWrite(JNIEnv *env, jobject thiz, jlong handle, jbyteArray data) {
    auto term = reinterpret_cast<GhosttyTerminal>(handle);
    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, NULL);

    std::vector<uint8_t> response;
    auto write_cb = [](void *ctx, const uint8_t *data, size_t len) {
        auto *vec = static_cast<std::vector<uint8_t> *>(ctx);
        vec->insert(vec->end(), data, data + len);
    };
    GhosttyWriter writer = { .write = write_cb, .context = &response };

    ghostty_terminal_vt_write(term, (const uint8_t *)bytes, len, writer);

    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);

    if (response.empty()) return nullptr;
    jbyteArray result = env->NewByteArray(response.size());
    env->SetByteArrayRegion(result, 0, response.size(), (const jbyte *)response.data());
    return result;
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeResize(JNIEnv *env, jobject thiz, jlong handle, jint cols, jint rows) {
    auto term = reinterpret_cast<GhosttyTerminal>(handle);
    ghostty_terminal_resize(term, (uint16_t)cols, (uint16_t)rows);
}

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeSnapshot(JNIEnv *env, jobject thiz, jlong handle, jobject buffer) {
    auto term = reinterpret_cast<GhosttyTerminal>(handle);
    auto *buf = (uint8_t *)env->GetDirectBufferAddress(buffer);
    jlong capacity = env->GetDirectBufferCapacity(buffer);
    if (!buf) return 0;

    size_t written = 0;
    GhosttyResult res = ghostty_terminal_render_state_snapshot(term, buf, (size_t)capacity, &written);
    if (res != GHOSTTY_SUCCESS) return 0;
    return (jint)written;
}

JNIEXPORT jbyteArray JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeEncodeKey(JNIEnv *env, jobject thiz, jlong handle, jint keyCode, jint action, jint metaState, jint unshiftedCodepoint, jbyteArray utf8) {
    auto term = reinterpret_cast<GhosttyTerminal>(handle);
    GhosttyKeyEncoder encoder;
    ghostty_key_encoder_new(NULL, &encoder);

    GhosttyKeyEvent event = GHOSTTY_INIT_SIZED(GhosttyKeyEvent);
    event.key = (GhosttyKey)keyCode;
    event.action = (GhosttyKeyAction)action;
    event.mods = (GhosttyMods)metaState;
    event.unshifted_codepoint = (uint32_t)unshiftedCodepoint;

    if (utf8 != nullptr) {
        jsize len = env->GetArrayLength(utf8);
        jbyte *bytes = env->GetByteArrayElements(utf8, NULL);
        event.utf8 = (const char *)bytes;
        event.utf8_len = (size_t)len;
        env->ReleaseByteArrayElements(utf8, bytes, JNI_ABORT);
    }

    char out_buf[128];
    size_t out_len = 0;
    ghostty_key_encoder_encode(encoder, event, out_buf, sizeof(out_buf), &out_len);
    ghostty_key_encoder_free(encoder);

    if (out_len == 0) return nullptr;
    jbyteArray result = env->NewByteArray(out_len);
    env->SetByteArrayRegion(result, 0, out_len, (const jbyte *)out_buf);
    return result;
}

}
