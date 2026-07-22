package kr.esob.fdms.util;

public class epdfconvert {

    public native int pdfconvert(String strDocumentPath, String strPDFPath);

    static 
    { 
        System.loadLibrary("e_pdf_convert_x64"); 
    }

    public static void main(String[] args) {
        int iRet = 0;
        epdfconvert epdf = new epdfconvert();

        System.out.println("strDocumentPath:\t\t" + args[0]);
        System.out.println("strPDFPath:\t\t\t" + args[1]);

        iRet = epdf.pdfconvert(args[0], args[1]);

        System.out.println("return value:\t\t\t" + iRet);
        
    }
	
	
}
