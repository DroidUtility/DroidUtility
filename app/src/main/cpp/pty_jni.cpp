#include <jni.h>
#include <pty.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/wait.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <cstring>
#include <cstdlib>
#include <android/log.h>

#define LOG_TAG "DroidPty"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static char **to_cstr_array(JNIEnv *env, jobjectArray arr) {
    jsize n = env->GetArrayLength(arr);
    char **out = (char **)calloc(n + 1, sizeof(char *));
    for (int i = 0; i < n; i++) {
        jstring s = (jstring)env->GetObjectArrayElement(arr, i);
        out[i] = strdup(env->GetStringUTFChars(s, nullptr));
    }
    out[n] = nullptr;
    return out;
}

extern "C" {

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeForkPty(
    JNIEnv *env, jobject thiz,
    jstring jBinary, jobjectArray jArgs, jobjectArray jEnvVars,
    jstring jCwd, jintArray jOutFd)
{
    const char *binary = env->GetStringUTFChars(jBinary, nullptr);
    const char *cwd    = env->GetStringUTFChars(jCwd, nullptr);
    char **argv = to_cstr_array(env, jArgs);
    char **envp = to_cstr_array(env, jEnvVars);

    int master_fd = -1, slave_fd = -1;
    struct winsize ws = { .ws_row = 24, .ws_col = 80 };

    if (openpty(&master_fd, &slave_fd, nullptr, nullptr, &ws) == -1) {
        LOGI("openpty failed: %s", strerror(errno));
        return -1;
    }

    pid_t pid = fork();
    if (pid < 0) {
        close(master_fd);
        close(slave_fd);
        return -1;
    }

    if (pid == 0) {
        close(master_fd);
        setsid();
        ioctl(slave_fd, TIOCSCTTY, 0);

        dup2(slave_fd, STDIN_FILENO);
        dup2(slave_fd, STDOUT_FILENO);
        dup2(slave_fd, STDERR_FILENO);
        if (slave_fd > STDERR_FILENO) close(slave_fd);

        chdir(cwd);
        execve(binary, argv, envp);
        _exit(127);
    }

    close(slave_fd);
    fcntl(master_fd, F_SETFD, FD_CLOEXEC);

    jint fds[2] = { master_fd, (jint)pid };
    env->SetIntArrayRegion(jOutFd, 0, 2, fds);

    env->ReleaseStringUTFChars(jBinary, binary);
    env->ReleaseStringUTFChars(jCwd, cwd);
    return 0;
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeSetWindowSize(
    JNIEnv *env, jobject thiz, jint fd, jint cols, jint rows)
{
    struct winsize ws = {
        .ws_row = (unsigned short)rows,
        .ws_col = (unsigned short)cols,
    };
    ioctl(fd, TIOCSWINSZ, &ws);
}

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeRead(
    JNIEnv *env, jobject thiz, jint fd, jbyteArray buf)
{
    jsize cap = env->GetArrayLength(buf);
    jbyte *raw = env->GetByteArrayElements(buf, nullptr);
    ssize_t n = read(fd, raw, cap);
    env->ReleaseByteArrayElements(buf, raw, 0);
    return (jint)n;
}

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeWrite(
    JNIEnv *env, jobject thiz, jint fd, jbyteArray buf)
{
    jsize len = env->GetArrayLength(buf);
    jbyte *raw = env->GetByteArrayElements(buf, nullptr);
    ssize_t n = write(fd, raw, len);
    env->ReleaseByteArrayElements(buf, raw, JNI_ABORT);
    return (jint)n;
}

JNIEXPORT void JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeCloseFd(
    JNIEnv *env, jobject thiz, jint fd)
{
    if (fd >= 0) close(fd);
}

JNIEXPORT jint JNICALL
Java_com_frostre1997_droidutility_terminal_Pty_nativeWaitFor(
    JNIEnv *env, jobject thiz, jint pid)
{
    int status = 0;
    waitpid((pid_t)pid, &status, 0);
    return WIFEXITED(status) ? WEXITSTATUS(status) : -1;
}

}
