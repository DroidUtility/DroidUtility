#include <jni.h>
#include <cstring>
#include <cstdint>
#include <vector>
#include <ghostty/vt.h>

struct Session {
    GhosttyTerminal terminal = nullptr;
    GhosttyRenderState render_state = nullptr;
    GhosttyRenderStateRowIterator row_iter = nullptr;
    GhosttyRenderStateRowCells row_cells = nullptr;
};

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeCreate(JNIEnv *env, jobject thiz, jint cols, jint rows) {
    auto *s = new Session();

    GhosttyTerminalOptions opts = {
        .cols = (uint16_t)cols,
        .rows = (uint16_t)rows,
        .max_scrollback = 1000
    };

    if (ghostty_terminal_new(NULL, &s->terminal, opts) != GHOSTTY_SUCCESS) {
        delete s;
        return 0;
    }
    ghostty_terminal_resize(s->terminal, (uint16_t)cols, (uint16_t)rows, 0, 0);

    if (ghostty_render_state_new(NULL, &s->render_state) != GHOSTTY_SUCCESS ||
        ghostty_render_state_row_iterator_new(NULL, &s->row_iter) != GHOSTTY_SUCCESS ||
        ghostty_render_state_row_cells_new(NULL, &s->row_cells) != GHOSTTY_SUCCESS) {
        if (s->render_state) ghostty_render_state_free(s->render_state);
        if (s->row_iter) ghostty_render_state_row_iterator_free(s->row_iter);
        if (s->row_cells) ghostty_render_state_row_cells_free(s->row_cells);
        ghostty_terminal_free(s->terminal);
        delete s;
        return 0;
    }

    return reinterpret_cast<jlong>(s);
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeFree(JNIEnv *env, jobject thiz, jlong handle) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (!s) return;
    if (s->row_cells) ghostty_render_state_row_cells_free(s->row_cells);
    if (s->row_iter) ghostty_render_state_row_iterator_free(s->row_iter);
    if (s->render_state) ghostty_render_state_free(s->render_state);
    if (s->terminal) ghostty_terminal_free(s->terminal);
    delete s;
}

JNIEXPORT jbyteArray JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeWrite(JNIEnv *env, jobject thiz, jlong handle, jbyteArray data) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (!s || !s->terminal) return nullptr;

    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    ghostty_terminal_vt_write(s->terminal, (const uint8_t *)bytes, (size_t)len);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    return nullptr;
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeResize(JNIEnv *env, jobject thiz, jlong handle, jint cols, jint rows) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (!s || !s->terminal) return;
    ghostty_terminal_resize(s->terminal, (uint16_t)cols, (uint16_t)rows, 0, 0);
}

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeSnapshot(JNIEnv *env, jobject thiz, jlong handle, jobject buffer) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (!s || !s->render_state || !s->terminal) return 0;

    if (ghostty_render_state_update(s->render_state, s->terminal) != GHOSTTY_SUCCESS) return 0;

    uint16_t cols = 0, rows = 0;
    ghostty_render_state_get(s->render_state, GHOSTTY_RENDER_STATE_DATA_COLS, &cols);
    ghostty_render_state_get(s->render_state, GHOSTTY_RENDER_STATE_DATA_ROWS, &rows);
    if (cols == 0 || rows == 0) return 0;

    GhosttyColorRgb default_fg = { 0, 255, 0 };
    GhosttyColorRgb default_bg = { 0, 0, 0 };
    ghostty_render_state_get(s->render_state, GHOSTTY_RENDER_STATE_DATA_COLOR_FOREGROUND, &default_fg);
    ghostty_render_state_get(s->render_state, GHOSTTY_RENDER_STATE_DATA_COLOR_BACKGROUND, &default_bg);

    auto *buf = (uint8_t *)env->GetDirectBufferAddress(buffer);
    jlong cap = env->GetDirectBufferCapacity(buffer);
    if (!buf) return 0;

    size_t needed = 4 + (size_t)cols * rows * 16;
    if (needed > (size_t)cap) return 0;

    buf[0] = cols & 0xFF;  buf[1] = (cols >> 8) & 0xFF;
    buf[2] = rows & 0xFF;  buf[3] = (rows >> 8) & 0xFF;
    size_t off = 4;

    int32_t default_fg_argb = (0xFF << 24) | (default_fg.r << 16) | (default_fg.g << 8) | default_fg.b;
    int32_t default_bg_argb = (0xFF << 24) | (default_bg.r << 16) | (default_bg.g << 8) | default_bg.b;

    if (ghostty_render_state_get(s->render_state, GHOSTTY_RENDER_STATE_DATA_ROW_ITERATOR, s->row_iter) != GHOSTTY_SUCCESS) {
        return 0;
    }

    int row_idx = 0;
    while (row_idx < rows && ghostty_render_state_row_iterator_next(s->row_iter)) {
        int col_idx = 0;

        if (ghostty_render_state_row_get(s->row_iter, GHOSTTY_RENDER_STATE_ROW_DATA_CELLS, s->row_cells) == GHOSTTY_SUCCESS) {
            while (col_idx < cols && ghostty_render_state_row_cells_next(s->row_cells)) {
                uint32_t cp = ' ';
                uint32_t glen = 0;
                if (ghostty_render_state_row_cells_get(s->row_cells, GHOSTTY_RENDER_STATE_ROW_CELLS_DATA_GRAPHEMES_LEN, &glen) == GHOSTTY_SUCCESS && glen > 0) {
                    uint32_t cps[8] = {0};
                    if (ghostty_render_state_row_cells_get(s->row_cells, GHOSTTY_RENDER_STATE_ROW_CELLS_DATA_GRAPHEMES_BUF, cps) == GHOSTTY_SUCCESS) {
                        cp = cps[0];
                    }
                }

                GhosttyColorRgb fg = default_fg;
                GhosttyColorRgb bg = default_bg;
                ghostty_render_state_row_cells_get(s->row_cells, GHOSTTY_RENDER_STATE_ROW_CELLS_DATA_FG_COLOR, &fg);
                ghostty_render_state_row_cells_get(s->row_cells, GHOSTTY_RENDER_STATE_ROW_CELLS_DATA_BG_COLOR, &bg);

                int32_t fg_argb = (0xFF << 24) | (fg.r << 16) | (fg.g << 8) | fg.b;
                int32_t bg_argb = (0xFF << 24) | (bg.r << 16) | (bg.g << 8) | bg.b;
                int32_t flags = 0;

                memcpy(buf + off, &cp, 4); off += 4;
                memcpy(buf + off, &fg_argb, 4); off += 4;
                memcpy(buf + off, &bg_argb, 4); off += 4;
                memcpy(buf + off, &flags, 4); off += 4;
                col_idx++;
            }
        }

        while (col_idx < cols) {
            uint32_t cp = ' ';
            int32_t flags = 0;
            memcpy(buf + off, &cp, 4); off += 4;
            memcpy(buf + off, &default_fg_argb, 4); off += 4;
            memcpy(buf + off, &default_bg_argb, 4); off += 4;
            memcpy(buf + off, &flags, 4); off += 4;
            col_idx++;
        }
        row_idx++;
    }

    while (row_idx < rows) {
        for (int c = 0; c < cols; c++) {
            uint32_t cp = ' ';
            int32_t flags = 0;
            memcpy(buf + off, &cp, 4); off += 4;
            memcpy(buf + off, &default_fg_argb, 4); off += 4;
            memcpy(buf + off, &default_bg_argb, 4); off += 4;
            memcpy(buf + off, &flags, 4); off += 4;
        }
        row_idx++;
    }

    return (jint)off;
}

JNIEXPORT jbyteArray JNICALL
Java_com_frostre1997_droidutility_terminal_GhosttyVt_nativeEncodeKey(JNIEnv *env, jobject thiz, jlong handle, jint keyCode, jint action, jint metaState, jint unshiftedCodepoint, jbyteArray utf8) {
    auto *s = reinterpret_cast<Session *>(handle);
    if (!s || !s->terminal) return nullptr;

    GhosttyKeyEncoder enc = nullptr;
    if (ghostty_key_encoder_new(NULL, &enc) != GHOSTTY_SUCCESS) return nullptr;
    ghostty_key_encoder_setopt_from_terminal(enc, s->terminal);

    GhosttyKeyEvent event = nullptr;
    if (ghostty_key_event_new(NULL, &event) != GHOSTTY_SUCCESS) {
        ghostty_key_encoder_free(enc);
        return nullptr;
    }

    ghostty_key_event_set_action(event, (GhosttyKeyAction)action);
    ghostty_key_event_set_key(event, (GhosttyKey)keyCode);
    ghostty_key_event_set_mods(event, (GhosttyMods)metaState);
    ghostty_key_event_set_unshifted_codepoint(event, (uint32_t)unshiftedCodepoint);

    if (utf8 != nullptr) {
        jsize len = env->GetArrayLength(utf8);
        jbyte *bytes = env->GetByteArrayElements(utf8, NULL);
        ghostty_key_event_set_utf8(event, (const char *)bytes, (size_t)len);
        env->ReleaseByteArrayElements(utf8, bytes, JNI_ABORT);
    }

    size_t required = 0;
    GhosttyResult r = ghostty_key_encoder_encode(enc, event, NULL, 0, &required);
    if (r != GHOSTTY_OUT_OF_SPACE || required == 0) {
        ghostty_key_event_free(event);
        ghostty_key_encoder_free(enc);
        return nullptr;
    }

    std::vector<char> out(required);
    size_t written = 0;
    r = ghostty_key_encoder_encode(enc, event, out.data(), out.size(), &written);

    ghostty_key_event_free(event);
    ghostty_key_encoder_free(enc);

    if (r != GHOSTTY_SUCCESS || written == 0) return nullptr;

    jbyteArray result = env->NewByteArray((jsize)written);
    env->SetByteArrayRegion(result, 0, (jsize)written, (const jbyte *)out.data());
    return result;
}

}
