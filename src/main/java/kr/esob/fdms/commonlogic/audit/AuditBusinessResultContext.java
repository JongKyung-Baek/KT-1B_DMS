package kr.esob.fdms.commonlogic.audit;

/**
 * Request-local result signal. Only an explicit boolean is retained; response
 * bodies and messages are never copied into the audit context.
 */
public final class AuditBusinessResultContext {
    public static final String REQUEST_ATTRIBUTE =
            AuditBusinessResultContext.class.getName() + ".success";

    private AuditBusinessResultContext() {
    }
}
