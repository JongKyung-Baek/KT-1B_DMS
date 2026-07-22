package epdf;

public class epdfconvert
{
    public native int pdfconvert(String strDocumentPath, String strPDFPath);
    public native int pdfmerge(String strDocumentPath, String strPDFPath);

    // 3D Convert
    // str3DPath : 3D뷰어 경로
    // ex : C:\workspaceStartupHub\OUT\3DFile\5db5b3uw3qvrdzc7plb0dmt65rbmri18_w-oshc66v3.pbo
    // strEncoingPath : 인코딩 아웃풋 패스
    // ex : C:\workspaceStartupHub\vServer\pdfview_express\public\VIZWeb3D\VIZCore\MODEL
    public native int convert3D(String str3DPath, String strEncoingPath);

    // 뷰어 호출
    // http://collabhub.iptime.org:7442/VIZWeb3D/index.html?file=5db5b3uw3qvrdzc7plb0dmt65rbmri18_w-oshc66v3/5db5b3uw3qvrdzc7plb0dmt65rbmri18_w-oshc66v3_wh.vizw
    // 파일이름 : 5db5b3uw3qvrdzc7plb0dmt65rbmri18_w-oshc66v3_wh.vizw
    // 파일명은 기존파일이름(확장자제거된이름)_wh.vizw

    static
    {
        try {
            // DLL 경로 로깅
            String dllPath = "C:\\Collabhub\\E_PDF\\e_pdf_convert_x64.dll";
            System.out.println("Attempting to load DLL from: " + dllPath);

            // DLL 파일 존재 여부 확인
            java.io.File dllFile = new java.io.File(dllPath);
            if (dllFile.exists()) {
                System.out.println("DLL file exists: " + dllFile.getAbsolutePath());
            } else {
                System.out.println("DLL file does not exist: " + dllFile.getAbsolutePath());
            }

            // DLL 로드 시도
            System.load(dllPath);
            System.out.println("DLL loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load DLL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error while loading DLL: " + e.getMessage());
            e.printStackTrace();
        }

        // 경우에 따라서 86 코드 써줌.
        // System.load("C:\\workspaceStartupHub\\FMDS\\src\\main\\resources\\static\\install\\e_pdf\\e_pdf_convert_x86.dll");
        // System.load("C:\\dev\\git\\collabhub\\src\\main\\resources\\static\\install\\e_pdf\\e_pdf_convert_x86.dll");

        // 서버용 윈도우에서 에러나서 위에 코드로 바꿈
        // System.load("C:\\dev\\git\\collabhub\\src\\main\\resources\\static\\install\\e_pdf\\e_pdf_convert_x64.dll");

        // 작동안되는 레거시 코드.
        // System.loadLibrary("e_pdf_convert_x64");
    }
}
