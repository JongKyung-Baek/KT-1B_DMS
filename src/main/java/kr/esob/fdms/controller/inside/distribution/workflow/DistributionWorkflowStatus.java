package kr.esob.fdms.controller.inside.distribution.workflow;

/**
 * Server-owned lifecycle for a technical-data distribution request.
 */
public enum DistributionWorkflowStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    CANCELLED
}
