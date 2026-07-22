<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>

<!-- <script>
  (function() {
        var gridId = "${gridId}";
        var reqType = "${reqType}";
        var objectType = "${objectType}";
        var distributeTypeCd = "${distributeTypeCd}";
        var selectedDataJsonBase64 = "${selectedDataJson}";

        function decodeHtmlEntities(value) {
                return $("<textarea/>").html(value || "").text();
        }

        function setStatus(rowIndex, text) {
                $("#dlStatus_" + rowIndex).text(text);
        }

        function renderRows(list) {
                var $tbody = $("#downloadProgressBody");
                $tbody.empty();

                for (var i = 0; i < list.length; i++) {
                        var name = list[i].orgFileNm || list[i].fileNm || "";
                        var tr = ""
                                + "<tr>"
                                + "<td style='padding:6px 8px; border-bottom:1px solid #eee;'>" + $("<div>").text(name).html() + "</td>"
                                + "<td id='dlStatus_" + i + "' style='padding:6px 8px; border-bottom:1px solid #eee;'>대기중</td>"
                                + "</tr>";
                        $tbody.append(tr);
                }
        }

        function normalizeSelectedRow(item) {
                if (!item) { return item; }

                var fileNmRaw = item.fileNm || "";
                if (typeof fileNmRaw === "string" && fileNmRaw.indexOf("<a") > -1) {
                        var $tmp = $("<div>").html(fileNmRaw);
                        var anchorText = $.trim($tmp.text());
                        if (anchorText) {
                                item.fileNm = anchorText;
                                if (!item.orgFileNm) {
                                        item.orgFileNm = anchorText;
                                }
                        }

                        var onclick = $tmp.find("a").attr("onclick") || "";
                        // openFile('DISTRIBUTION','DRAWING','requestNo','docSeq','fileNo','protectYn')
                        var m = onclick.match(/openFile\(\s*'[^']*'\s*,\s*'[^']*'\s*,\s*'([^']*)'\s*,\s*'([^']*)'/);
                        if (m) {
                                if ((!item.requestNo || item.requestNo === "undefined") && m[1]) {
                                        item.requestNo = m[1];
                                }
                                if ((!item.docSeq || item.docSeq === "undefined" || item.docSeq === "null") && m[2]) {
                                        item.docSeq = m[2];
                                }
                        }
                }
                return item;
        }

        $(function() {
                $("#popupDialog").closest(".ui-dialog").addClass("popup-common popup-download-progress");

                var selectedData = [];
                var selectedDataJson = "";
                var selectedDataJsonDecoded = decodeHtmlEntities(selectedDataJsonBase64);
                if (selectedDataJsonDecoded) {
                        try {
                                selectedDataJson = decodeURIComponent(escape(atob(selectedDataJsonDecoded)));
                                selectedData = JSON.parse(selectedDataJson);
                        } catch (e) {
                                console.log(e);
                        }
                }

                // backward compatibility: 전달값 없을 때만 기존 그리드 참조
                if (!selectedData || selectedData.length < 1) {
                        var selectedRows = $("#" + gridId).jqGrid('getGridParam', 'selarrrow') || [];
                        selectedData = [];
                        $.each(selectedRows, function(_, rowId) {
                                selectedData.push($("#" + gridId).jqGrid('getRowData', rowId));
                        });
                }

                $.each(selectedData, function(idx, item) {
                        item = normalizeSelectedRow(item);
                        if (!item.docSeq || item.docSeq === "undefined" || item.docSeq === "null") {
                                if (item.delvyCnfirmDocSeq) {
                                        item.docSeq = item.delvyCnfirmDocSeq;
                                } else if (item.dataOfferDocSeq) {
                                        item.docSeq = item.dataOfferDocSeq;
                                } else if (item.docseq) {
                                        item.docSeq = item.docseq;
                                } else if (item.DOC_SEQ) {
                                        item.docSeq = item.DOC_SEQ;
                                }
                        }
                });

                if (selectedData.length < 1) {
                        alertMessage(g_msg('msg.noSelectData'));
                        return;
                }

                var param = {
                        list: selectedData,
                        reqType: reqType,
                        objectType: objectType,
                        distributeTypeCd: distributeTypeCd
                };

                callAjax(param, '/common/updown/downloadData', function(response) {
                        var list = (response && response.list) ? response.list : [];
                        renderRows(list);

                        callDownload(response, {
                                showSuccessAlert: false,
                                reloadOnSuccess: false,
                                onStatusChange: function(evt) {
                                        if (typeof evt.index === "number" && evt.status) {
                                                setStatus(evt.index, evt.status);
                                        }
                                },
                                onComplete: function(summary) {
                                        if (summary.fail > 0) {
                                                $("#downloadSummary").text("완료(실패 " + summary.fail + "건)");
                                        } else {
                                                $("#downloadSummary").text("완료");
                                        }
                                }
                        });
                }, 'json');
        });
  })();
</script> -->

  <script>
  (function() {
      var gridId = "${gridId}";
      var reqType = "${reqType}";
      var objectType = "${objectType}";
      var distributeTypeCd = "${distributeTypeCd}";
      var selectedDataJsonBase64 = "${selectedDataJson}";

      function statusText(status) {
          if (status === "QUEUED") return "대기";
          if (status === "DOWNLOADING") return "다운로드중";
          if (status === "SENT_TO_WS") return "전송완료";
          if (status === "COMPLETED") return "완료";
          if (status === "FAILED") return "실패";
          return status || "-";
      }

      function sanitizeRowKey(value) {
          return String(value || "").replace(/[^A-Za-z0-9_-]/g, "_");
      }

      function deriveDownloadRowKey(item, fallbackIndex) {
          if (!item) return "row_" + fallbackIndex;

          var objectType = String(item.objectType || objectType || "").toUpperCase();
          if (item.fileSeq) return "row_" + sanitizeRowKey(item.fileSeq);
          if (item.requestNo || item.docSeq) {
              return "row_" + sanitizeRowKey([
                  item.requestNo || "",
                  item.docSeq || ""
              ].join("_"));
          }
          if (objectType === "SW" || objectType === "SECP" || objectType === "SECP_PARTDOC") {
              if (item.fileSeq) return "row_" + sanitizeRowKey(item.fileSeq);
          }
          if (objectType === "DOC" || objectType === "DRAWING") {
              if (item.fileNm) return "row_" + sanitizeRowKey(item.fileNm);
          }

          if (item.fileSeq) return "row_" + sanitizeRowKey(item.fileSeq);
          if (item.fileNm) return "row_" + sanitizeRowKey(item.fileNm);
          if (item.orgFileNm) return "row_" + sanitizeRowKey(item.orgFileNm);
          if (item.requestNo || item.docSeq) {
              return "row_" + sanitizeRowKey([
                  item.requestNo || "",
                  item.docSeq || "",
                  fallbackIndex
              ].join("_"));
          }
          return "row_" + fallbackIndex;
      }

      function setStatusByRow(rowKey, status) {
          var nextText = statusText(status);
          var $cell = $("#dlStatus_" + rowKey);
          if ($cell.text() === nextText) {
              return;
          }
          $cell.text(nextText);
      }

      function decodeSelectedData() {
          if (!selectedDataJsonBase64) return [];
          try {
              var json = decodeURIComponent(escape(atob(selectedDataJsonBase64)));
              return JSON.parse(json) || [];
          } catch (e) {
              console.log(e);
              return [];
          }
      }

      function normalizeSelectedRow(item) {
          if (!item) return item;

          function pick() {
              for (var i = 0; i < arguments.length; i++) {
                  var v = arguments[i];
                  if (v !== undefined && v !== null && String(v) !== "" && String(v) !== "undefined" && String(v) !== "null") {
                      return String(v);
                  }
              }
              return "";
          }

          item.requestNo = pick(item.requestNo, item.requestno, item.REQUEST_NO);
//          item.dataNo = pick(item.dataNo, item.DATA_NO, item.objectNo, item.objectno, item.OBJECT_NO);
          item.dataSeq = pick(item.dataSeq, item.DATA_SEQ, item.docSeq, item.docseq, item.DOC_SEQ, item.dataOfferDocSeq, item.delvyCnfirmDocSeq);
          item.docSeq = pick(item.docSeq, item.docseq, item.DOC_SEQ, item.dataOfferDocSeq, item.delvyCnfirmDocSeq);
          item.fileSeq = pick(item.fileSeq, item.fileseq, item.FILE_SEQ);
          item.objectType = pick(item.objectType, item.objecttype, item.OBJECT_TYPE, objectType);
          item.fileNm = pick(item.fileNm, item.filenm, item.FILE_NM);
          item.orgFileNm = pick(item.orgFileNm, item.orgfilenm, item.ORG_FILE_NM, item.fileNm);
          item.folderName = pick(item.folderName, item.foldername, item.FOLDER_NAME, item.requestNo);
          return item;
      }

      function renderRows(list) {
          var $tbody = $("#downloadProgressBody");
          $tbody.empty();
          for (var i = 0; i < list.length; i++) {
              var name = list[i].orgFileNm || list[i].fileNm || "";
              var rowKey = list[i]._downloadRowKey || deriveDownloadRowKey(list[i], i);
              list[i]._downloadRowKey = rowKey;
              var esc = $("<div>").text(name).html();
              $tbody.append(
                  "<tr>" +
                  "<td style='padding:8px;border-bottom:1px solid #eee;'>" + esc + "</td>" +
                  "<td id='dlStatus_" + rowKey + "' style='padding:8px;border-bottom:1px solid #eee;'>대기</td>" +
                  "</tr>"
              );
          }
      }

      function closeDownloadPopup() {
          if (window.downloadSeqList && window.downloadSeqList.length > 0) {
              for (var i = 0; i < window.downloadSeqList.length; i++) {
                  cleanupDownload(window.downloadSeqList[i]);
              }
          }
          closePopup('popupDialog');
      }

      window.closeDownloadPopup = closeDownloadPopup;

      $(function() {
          $("#popupDialog").closest(".ui-dialog").addClass("popup-common popup-download-progress");

          var selectedData = decodeSelectedData();
          if (!selectedData || selectedData.length < 1) {
              alertMessage(g_msg('msg.noSelectData'));
              return;
          }

          for (var i = 0; i < selectedData.length; i++) {
              selectedData[i] = normalizeSelectedRow(selectedData[i]);
              selectedData[i]._downloadRowKey = deriveDownloadRowKey(selectedData[i], i);
          }

          renderRows(selectedData);

          window.onDownloadStatusUpdate = function(evt) {
              if (!evt || !evt.meta) return;
              if (evt.meta.rowKey === undefined || evt.meta.rowKey === null || evt.meta.rowKey === "") return;
              setStatusByRow(evt.meta.rowKey, evt.status);
          };

          var param = {
              list: selectedData,
              reqType: reqType,
              objectType: objectType,
              distributeTypeCd: distributeTypeCd
          };
          // common.js(v2/start)에서 response.list에는 objectType이 비어 있을 수 있어 fallback으로 사용
          window.downloadRequestObjectType = objectType;

          callAjax(param, '/common/updown/downloadData', function(response) {
              if (response && response.list && response.list.length) {
                  var rowKeyMap = {};
                  for (var i = 0; i < selectedData.length; i++) {
                      rowKeyMap[selectedData[i]._downloadRowKey] = selectedData[i]._downloadRowKey;
                  }
                  for (var i = 0; i < response.list.length; i++) {
                      if (!response.list[i].objectType) {
                          response.list[i].objectType = objectType;
                      }
                      response.list[i]._downloadRowKey = deriveDownloadRowKey(response.list[i], i);
                  }
                  for (var i = 0; i < response.list.length; i++) {
                      if (rowKeyMap[response.list[i]._downloadRowKey]) {
                          continue;
                      }
                      for (var j = 0; j < selectedData.length; j++) {
                          if (!selectedData[j] || !selectedData[j]._downloadRowKey) {
                              continue;
                          }
                          if (selectedData[j].fileSeq && response.list[i].fileSeq && selectedData[j].fileSeq === response.list[i].fileSeq) {
                              response.list[i]._downloadRowKey = selectedData[j]._downloadRowKey;
                              break;
                          }
                          if (selectedData[j].requestNo && response.list[i].requestNo
                                  && selectedData[j].docSeq && response.list[i].docSeq
                                  && selectedData[j].requestNo === response.list[i].requestNo
                                  && selectedData[j].docSeq === response.list[i].docSeq) {
                              response.list[i]._downloadRowKey = selectedData[j]._downloadRowKey;
                              break;
                          }
//                          if (selectedData[j].dataNo && response.list[i].dataNo && selectedData[j].dataNo === response.list[i].dataNo) {
//                              response.list[i]._downloadRowKey = selectedData[j]._downloadRowKey;
//                              break;
//                          }
                          if (selectedData[j].fileNm && response.list[i].fileNm && selectedData[j].fileNm === response.list[i].fileNm) {
                              response.list[i]._downloadRowKey = selectedData[j]._downloadRowKey;
                              break;
                          }
                      }
                  }
              }
              callDownload(response);
          }, 'json', true);
      });
  })();
  </script>

<div class="dialogContent requestStatusPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
    <div class="popupHero">
        <h2>다운로드 진행상태</h2>
        <p>선택한 파일의 다운로드 진행 상태를 확인합니다.</p>
    </div>

    <div class="section popupCard">
        <div style="margin-bottom:8px; font-weight:600;">진행 현황 <span id="downloadSummary"></span></div>
        <div class="fileDownloadList" style="max-height:360px; overflow:auto;">
            <table style="width:100%; border-collapse:collapse;">
                <thead>
                    <tr>
                        <th style="text-align:left; padding:8px; border-bottom:1px solid #ddd;">파일명</th>
                        <th style="text-align:left; padding:8px; border-bottom:1px solid #ddd; width:140px;">진행상태</th>
                    </tr>
                </thead>
                <tbody id="downloadProgressBody"></tbody>
            </table>
        </div>
    </div>
</div>

<div class="dialogBtnSet">
        <div class="left"></div>
        <div class="right">
                <custom:popupButton function="closeDownloadPopup()" name="close" label="btn.close" id="close"/>
        </div>
</div>
