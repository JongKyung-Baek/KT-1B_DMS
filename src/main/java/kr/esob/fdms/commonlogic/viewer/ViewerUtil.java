package kr.esob.fdms.commonlogic.viewer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ViewerUtil {

	/**
	 * Svg파일 분할
	 * @param svgFilePath
	 * @return
	 */
	public static List<String> executeSvgFileParser(String svgFilePath) {
		List<String> fileList = new ArrayList<String>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			File fileSvg = new File(svgFilePath);
			Document doc = docBuilder.parse(fileSvg);
			Element element = doc.getDocumentElement();

			// svg 태그를 기준으로 분할
			NodeList nodeList = element.getElementsByTagName("svg");

			if(nodeList != null && nodeList.getLength() > 0) {
				String filePath = fileSvg.getParent();
				String fileName = fileSvg.getName();
				String fileTitle = fileName.substring(0, fileName.lastIndexOf("."));
				String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

				// 분할된 파일명에 001, 002 등의 파일 번호를 붙이기 위함
				DecimalFormat numberFormatter = new DecimalFormat("000");

				if(fileExtension.equalsIgnoreCase("SVG")) {
					fileExtension = "." + fileExtension;
				}
				else {
					fileExtension = "";
				}

				int intNodeSize = nodeList.getLength();
				TransformerFactory transFactory = TransformerFactory.newInstance();
				Transformer trans = transFactory.newTransformer();
				DOMSource domSource = new DOMSource();

				for(int i=0; i<intNodeSize; i++) {
					String tempFileName = fileTitle + "_" + numberFormatter.format(i+1) + fileExtension;
					String tempFilePath = filePath + File.separator + tempFileName;
					StreamResult file = new StreamResult(new File(tempFilePath));
					domSource = new DOMSource(nodeList.item(i));
					trans.transform(domSource, file);

					fileList.add(tempFilePath);
				}
			}
		}
		catch(Exception e) {
			fileList.clear();
			e.printStackTrace();
		}

		return fileList;
	}
}
