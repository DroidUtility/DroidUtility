#include <stddef.h>

typedef struct {
    unsigned long ti_module;
    unsigned long ti_offset;
} tls_index;

extern void *__emutls_get_address(void *);

void *__tls_get_addr(tls_index *ti) {
    return __emutls_get_address(ti);
}
