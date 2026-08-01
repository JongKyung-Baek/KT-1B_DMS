package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

/**
 * Client-supplied reference to one technical-data document. File identifiers
 * are deliberately absent: the server always expands the document to its one
 * main file and every active auxiliary file.
 */
@Getter
@Setter
public class DistributionRequestDocumentRef {
    private String objectId;
}
