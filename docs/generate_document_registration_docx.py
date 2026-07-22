from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from xml.sax.saxutils import escape
from PIL import Image, ImageDraw, ImageFont


BASE_DIR = Path(__file__).resolve().parent
ASSET_DIR = BASE_DIR / "document-registration-guide-assets"
OUT_FILE = BASE_DIR / "KT-1B_문서등록_조회_가이드.docx"


def font(size, bold=False):
    candidates = [
        Path("C:/Windows/Fonts/malgunbd.ttf" if bold else "C:/Windows/Fonts/malgun.ttf"),
        Path("C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf"),
    ]
    for path in candidates:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


FONT_12 = font(12)
FONT_14 = font(14)
FONT_16 = font(16)
FONT_18 = font(18, True)
FONT_22 = font(22, True)
FONT_28 = font(28, True)


def rounded(draw, box, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def label(draw, xy, text, fill=(35, 45, 65), fnt=FONT_14):
    draw.text(xy, text, fill=fill, font=fnt)


def header(draw, title):
    draw.rectangle((0, 0, 1360, 72), fill=(24, 42, 68))
    label(draw, (28, 21), "KT-1B 기술자료관리시스템", (255, 255, 255), FONT_22)
    label(draw, (1120, 25), "문서 배포요청", (210, 225, 245), FONT_16)
    label(draw, (28, 92), title, (20, 32, 48), FONT_28)


def draw_button(draw, box, text, fill=(31, 111, 235), outline=None):
    rounded(draw, box, 8, fill, outline or fill)
    tw = draw.textlength(text, font=FONT_14)
    x = box[0] + ((box[2] - box[0]) - tw) / 2
    y = box[1] + ((box[3] - box[1]) - 17) / 2
    label(draw, (x, y), text, (255, 255, 255), FONT_14)


def callout(draw, box, text):
    draw.rounded_rectangle(box, radius=10, outline=(230, 45, 45), width=4)
    x1, y1, x2, _ = box
    bubble = (x1, max(16, y1 - 46), min(1325, x1 + 360), max(48, y1 - 10))
    rounded(draw, bubble, 8, (230, 45, 45))
    label(draw, (bubble[0] + 12, bubble[1] + 9), text, (255, 255, 255), FONT_14)


def save_main_list(path):
    img = Image.new("RGB", (1360, 820), (244, 247, 251))
    draw = ImageDraw.Draw(img)
    header(draw, "1. 문서 등록 후 문서 배포요청 목록에서 확인")

    rounded(draw, (26, 145, 294, 770), 10, (255, 255, 255), (218, 225, 235))
    label(draw, (48, 166), "IOC", (35, 45, 65), FONT_18)
    tree = ["전체", "SP", "D0", "  D0-01", "  D0-02", "D1", "  D1-01", "D2"]
    y = 205
    for item in tree:
        fill = (235, 242, 255) if item.strip() == "D0-01" else (255, 255, 255)
        rounded(draw, (44, y - 5, 270, y + 28), 6, fill)
        label(draw, (58, y), item, (35, 45, 65), FONT_14)
        y += 39

    rounded(draw, (320, 145, 1328, 770), 10, (255, 255, 255), (218, 225, 235))
    label(draw, (344, 166), "문서 배포요청 목록", (35, 45, 65), FONT_18)
    draw_button(draw, (1146, 158, 1230, 194), "등록")
    draw_button(draw, (1238, 158, 1304, 194), "조회", (75, 85, 105))

    columns = ["상태", "문서번호", "문서제목", "등록자", "발행일", "주파일"]
    xs = [344, 454, 690, 940, 1050, 1160]
    widths = [90, 220, 235, 90, 95, 130]
    draw.rectangle((342, 224, 1304, 264), fill=(238, 242, 248), outline=(218, 225, 235))
    for x, w, col in zip(xs, widths, columns):
        label(draw, (x + 10, 236), col, (35, 45, 65), FONT_14)
        draw.line((x + w, 224, x + w, 744), fill=(225, 230, 238))

    rows = [
        ("승인진행중", "K8-IOC-D0-001-004", "전장 인터페이스 관리계획서", "홍길동", "2026-07-07", "문서.pdf"),
        ("승인완료", "K8-IOC-D0-001-003", "시험 절차서", "김가온", "2026-07-01", "절차서.pdf"),
        ("승인완료", "K8-IOC-D1-002-021", "품질 확인서", "박민수", "2026-06-22", "확인서.pdf"),
    ]
    y = 264
    for idx, row in enumerate(rows):
        fill = (255, 255, 255) if idx % 2 == 0 else (250, 252, 255)
        draw.rectangle((342, y, 1304, y + 54), fill=fill, outline=(232, 236, 243))
        for x, value in zip(xs, row):
            color = (30, 98, 210) if value.startswith("K8-IOC") else (50, 60, 78)
            label(draw, (x + 10, y + 18), value, color, FONT_14)
        y += 54

    callout(draw, (452, 266, 674, 316), "문서번호를 클릭")
    img.save(path)


def save_file_popup(path):
    img = Image.new("RGB", (1360, 820), (238, 242, 248))
    draw = ImageDraw.Draw(img)
    header(draw, "2. 파일 정보 팝업에서 등록 파일 확인")

    rounded(draw, (250, 132, 1110, 764), 12, (255, 255, 255), (206, 216, 230), 2)
    draw.rectangle((250, 132, 1110, 190), fill=(250, 252, 255), outline=(206, 216, 230))
    label(draw, (282, 152), "파일 정보", (20, 32, 48), FONT_22)
    label(draw, (282, 204), "문서번호: K8-IOC-D0-001-004", (45, 58, 78), FONT_16)

    sections = [
        (242, "승인자 승인 상태", ["이름", "상태", "코멘트"], [["홍길동", "대기", "승인하였습니다"], ["김가온", "승인", "확인 완료"]]),
        (416, "주파일 정보", ["파일순번", "파일명", "파일크기(Byte)"], [["1", "전장_인터페이스_관리계획서.pdf", "1,024,300"]]),
        (570, "보조파일 정보", ["파일순번", "파일명", "파일크기(Byte)"], [["2", "참고자료.xlsx", "84,112"]]),
    ]
    for top, title, cols, rows in sections:
        rounded(draw, (282, top, 1078, top + 120), 8, (255, 255, 255), (220, 226, 236))
        draw.rectangle((282, top, 1078, top + 40), fill=(247, 250, 253), outline=(220, 226, 236))
        label(draw, (300, top + 12), title, (35, 45, 65), FONT_16)
        draw_button(draw, (970, top + 7, 1056, top + 33), "다운로드", (75, 85, 105))
        col_x = [304, 430, 810]
        for x, c in zip(col_x, cols):
            label(draw, (x, top + 52), c, (75, 85, 105), FONT_12)
        row_y = top + 78
        for row in rows:
            label(draw, (304, row_y), row[0], (50, 60, 78), FONT_14)
            color = (30, 98, 210) if "." in row[1] else (50, 60, 78)
            label(draw, (430, row_y), row[1], color, FONT_14)
            label(draw, (810, row_y), row[2], (50, 60, 78), FONT_14)
            row_y += 30

    callout(draw, (424, 489, 710, 525), "파일명을 클릭")
    img.save(path)


def save_viewer(path):
    img = Image.new("RGB", (1360, 820), (230, 234, 240))
    draw = ImageDraw.Draw(img)
    header(draw, "3. 뷰어에서 등록 문서 열람")

    rounded(draw, (58, 132, 1302, 760), 8, (255, 255, 255), (204, 214, 228), 2)
    draw.rectangle((58, 132, 1302, 182), fill=(32, 42, 58), outline=(32, 42, 58))
    label(draw, (82, 148), "PDF Viewer", (255, 255, 255), FONT_18)
    for i, t in enumerate(["확대", "축소", "다운로드", "인쇄"]):
        draw_button(draw, (986 + i * 76, 142, 1050 + i * 76, 172), t, (70, 82, 100))

    rounded(draw, (345, 212, 1015, 720), 4, (252, 252, 252), (185, 195, 210))
    label(draw, (390, 250), "전장 인터페이스 관리계획서", (20, 32, 48), FONT_28)
    draw.line((390, 302, 970, 302), fill=(210, 218, 230), width=2)
    label(draw, (390, 336), "문서번호: K8-IOC-D0-001-004", (50, 60, 78), FONT_16)
    label(draw, (390, 374), "등록자: 홍길동", (50, 60, 78), FONT_16)
    label(draw, (390, 412), "발행일: 2026-07-07", (50, 60, 78), FONT_16)
    for y in [480, 522, 564, 606]:
        draw.rectangle((390, y, 970, y + 14), fill=(225, 231, 240))

    callout(draw, (344, 210, 1016, 722), "등록한 문서가 뷰어에 표시됨")
    img.save(path)


def paragraph(text="", style=None):
    style_xml = f'<w:pPr><w:pStyle w:val="{style}"/></w:pPr>' if style else ""
    return f"<w:p>{style_xml}<w:r><w:t>{escape(text)}</w:t></w:r></w:p>"


def bullet(text):
    return (
        '<w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>'
        f"<w:r><w:t>{escape(text)}</w:t></w:r></w:p>"
    )


def image_paragraph(rid, name, cx=5486400, cy=3301200):
    return f"""
    <w:p>
      <w:r>
        <w:drawing>
          <wp:inline distT="0" distB="0" distL="0" distR="0" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
            <wp:extent cx="{cx}" cy="{cy}"/>
            <wp:docPr id="{rid[3:]}" name="{escape(name)}"/>
            <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
                  <pic:nvPicPr><pic:cNvPr id="0" name="{escape(name)}"/><pic:cNvPicPr/></pic:nvPicPr>
                  <pic:blipFill><a:blip r:embed="{rid}"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
                  <pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr>
                </pic:pic>
              </a:graphicData>
            </a:graphic>
          </wp:inline>
        </w:drawing>
      </w:r>
    </w:p>
    """


def build_document_xml():
    body = []
    body.append(paragraph("KT-1B 문서 등록 후 조회 가이드", "Title"))
    body.append(paragraph("문서를 등록한 뒤 목록에서 문서번호를 클릭하고, 파일 정보 팝업에서 파일명을 클릭해 뷰어로 확인하는 절차입니다."))
    body.append(paragraph("1. 문서 등록", "Heading1"))
    body.append(bullet("문서 배포요청 화면(`/inside/distribution/docRequest/`)에서 등록 버튼을 클릭합니다."))
    body.append(bullet("등록 정보 팝업에서 문서제목, Function Code1/2, 배포 대상, 승인자, 주파일을 입력한 뒤 등록합니다."))
    body.append(paragraph("2. 등록 문서 확인", "Heading1"))
    body.append(bullet("등록 후 같은 문서 배포요청 목록에서 등록한 문서를 조회합니다."))
    body.append(bullet("목록에서 문서번호를 클릭하면 파일 정보 팝업이 열립니다."))
    body.append(image_paragraph("rId1", "문서 배포요청 목록"))
    body.append(paragraph("3. 파일 정보 팝업", "Heading1"))
    body.append(bullet("파일 정보 팝업에서 승인 상태, 주파일, 보조파일을 확인합니다."))
    body.append(bullet("주파일 정보 영역의 파일명을 클릭하면 뷰어가 호출됩니다. 다운로드가 필요하면 행 선택 후 다운로드 버튼을 클릭합니다."))
    body.append(image_paragraph("rId2", "파일 정보 팝업"))
    body.append(paragraph("4. 뷰어에서 문서 열람", "Heading1"))
    body.append(bullet("파일명을 클릭하면 등록된 문서가 뷰어에서 열린다."))
    body.append(bullet("뷰어에서 문서 내용을 확인하고, 필요 시 확대/축소, 다운로드, 인쇄 기능을 사용한다."))
    body.append(image_paragraph("rId3", "문서 뷰어"))
    body.append(paragraph("5. 핵심 경로 요약", "Heading1"))
    body.append(bullet("목록 화면: /inside/distribution/docRequest/"))
    body.append(bullet("등록 팝업: /inside/distribution/docRequest/docRegisterPopup"))
    body.append(bullet("파일 정보 팝업: /inside/distribution/docRequest/docFilePopup"))
    body.append(bullet("뷰어 호출: 파일 정보 팝업에서 파일명 클릭"))
    body.append(
        '<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="850" w:right="850" w:bottom="850" w:left="850" w:header="708" w:footer="708" w:gutter="0"/></w:sectPr>'
    )
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>{''.join(body)}</w:body>
</w:document>"""


def build_styles_xml():
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Malgun Gothic" w:eastAsia="Malgun Gothic"/><w:sz w:val="21"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:rPr><w:rFonts w:ascii="Malgun Gothic" w:eastAsia="Malgun Gothic"/><w:b/><w:sz w:val="36"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:rPr><w:rFonts w:ascii="Malgun Gothic" w:eastAsia="Malgun Gothic"/><w:b/><w:sz w:val="28"/></w:rPr></w:style>
</w:styles>"""


def build_numbering_xml():
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0">
    <w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val="•"/><w:lvlJc w:val="left"/></w:lvl>
  </w:abstractNum>
  <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
</w:numbering>"""


def write_docx(images):
    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rIdDoc" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/step1-main-list.png"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/step2-file-popup.png"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/step3-viewer.png"/>
  <Relationship Id="rIdStyles" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rIdNumbering" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
</Relationships>"""
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
</Types>"""

    with ZipFile(OUT_FILE, "w", ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types)
        zf.writestr("_rels/.rels", rels)
        zf.writestr("word/document.xml", build_document_xml())
        zf.writestr("word/styles.xml", build_styles_xml())
        zf.writestr("word/numbering.xml", build_numbering_xml())
        zf.writestr("word/_rels/document.xml.rels", doc_rels)
        for image_path in images:
            zf.write(image_path, f"word/media/{image_path.name}")


def main():
    ASSET_DIR.mkdir(exist_ok=True)
    images = [
        ASSET_DIR / "step1-main-list.png",
        ASSET_DIR / "step2-file-popup.png",
        ASSET_DIR / "step3-viewer.png",
    ]
    save_main_list(images[0])
    save_file_popup(images[1])
    save_viewer(images[2])
    write_docx(images)
    print(OUT_FILE)


if __name__ == "__main__":
    main()
