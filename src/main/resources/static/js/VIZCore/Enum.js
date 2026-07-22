/**
 * @author jhjang@softhills.net
 */

const CLIPPING_MODES = {
    X: 'X',
    Y: 'Y',
    Z: 'Z',
    BOX: 'Box'
};

const RENDER_MODES = {
    Wireframe: 'Wireframe',
    Smooth: 'Smooth',
    SmoothEdge: 'SmoothEdge',
    Flat: 'Flat',
    HiddenLine: 'HiddenLine',
    HiddenLine_Elimination: 'HiddenLine_Elimination',
    Xray: 'Xray'
};

const RENDER_MODES_TEXT = {
    Wireframe: '와이어프레임',
    Smooth: '부드러운음영',
    Flat: '거친음영'
};

const EVENT_TYPES = {
    View: {
        Init: 'View.Init',
        Loading_Complete: 'View.Loading_Complete'
    },
    Model: {
        Select: 'Model.Select'
    },
    Data: {
        Selected: 'Data.Selected',
        Deselect_All:'Data.Deselect_All'
    },
    Toolbar: {
        Show: 'Toolbar.Show',
        Hide: 'Toolbar.Hide'
    },
    Progress: {
        Percentage: 'Progress.Percentage'
    },
    Control: {
        Changed: 'Control.Changed'
    },
    Pivot: {
        DrawRender: 'Pivot.DrawRender'
    },
    Keyboard: {
        Down: 'Keyboard.Down'
    },
    Keyboard_SystemKey: {
        Down: 'Keyboard.Down.SystemKey'
    }
};

const MEASURE_UNIT = {
    mm: 'mm',
    cm: 'cm',
    m : 'm',
    inch: 'inch'
};

const ACCUMULATION_UNIT = {
    //mm: 0,
    //cm: 1,
    //m: 2,
    //inch: 3
    UNKNOWN : 0,
    MICROMETERS : 1,
    MILLIMETERS : 2,
    CENTIMETERS : 3,
    DECIMETERS : 4,
    METERS : 5,
    KILOMETERS : 6,
    INCHES : 7,
    FEET : 8,
    YARDS : 9,
    MILES : 10,
    MILS : 11
};

const PROGRESS_TYPES = {
    File_Downloading: 0,
    Data_Loading: 1,
    Edge_Loading: 2
};

const VIEW_MODES = {
    PlusX: 0,
    MinusX: 1,
    PlusY: 2,
    MinusY: 3,
    PlusZ: 4,
    MinusZ: 5,
    PlusISO: 6,
    MinusISO: 7
};

const PROJECTION_MODES = {
    Orthographic: 0,
    Perspective: 1
};

const ENTITY_TYPES = {
    EntUnknown : 0,
    EntFileHeader : 100,

    EntAssembly : 500,
    EntPart : 501,
    EntCurveBSpline: 502,
    EntSurfNurbs: 503,
    EntRefNode: 504,
    EntSurfGeneric: 505,
    EntInstCurve: 506,
    EntBody: 507,
    EntCurveLine: 508,
    EntCurveCircle: 509,
    EntMeshBlock: 510,
    EntBinaryBlock: 511,
    EntCFLBody: 512,

    EntFolder: 513,
    EntPoint: 514,
    EntPolyline: 515,
    EntCircle: 516,

    BST_STYLING_MODEL : 800,	
    BST_DRAWING_MODEL : 900,	

    BST_ANALYSIS : 1000,	
    BST_INSPECTION : 2000,
    EntEdgeBlock : 3000,
};

const SELECT_UNIT = {
    Body: 0,
    Part: 1,
    Assembly: 2,
    Level: 3
};

const REVIEW_TYPES = {
    NONE: 0,
    RK_MEASURE_POS: 1,
    RK_MEASURE_DISTANCE: 2,
    RK_MEASURE_ANGLE: 3,
    RK_SURFACE_NOTE: 4,
    RK_2D_NOTE: 5,
    RK_3D_NOTE: 6,
    RK_PATH : 7
};

const BROWSER_TYPES = {
    Unknown:0,
    Internet_Explorer: 1,
    Edge: 2,
    Chrome: 3,
    Firefox: 4,
    Safari: 5,
    Opera: 6
};

const PLATFORM_TYPES = {
    PC: 0,
    Mobile: 1
};

const THEMA_TYPES = {
    Basic: 0,
    Splitter: 1
};

const DRAWING_TYPES = {
    NONE  : 0,
    FREE: 1,
    LINE : 2,
    QUADRANGLE: 3,
    CIRCLE: 4
};

const PROCESS_TYPES = {
    NONE: 0,
    MOVE: 1
};

const SHADER_TYPES = {
    PHONG: 0,
    PBR: 1,
    PICKING: 2,
    TEXTURE2D: 3,
    BASIC2D: 4,
    FXAA: 5,
    DEPTH: 6,
    EDGE: 7,
    BACKGROUND: 8,
    SKYBOX: 9,
    PREFILTEREDENVIRONMENT : 10
};

const FB_RENDER_TYPES = {
    MAIN: 0,
    UI: 1,
    PICK: 2,
    DEPTH: 3,
    CUSTOM: 4,
    SELECTED: 5,
    LOD:6
};

const CONTROL_STATE = {
    NORMAL: 0,
    MARKUP: 1,
    WALKTHROUGH : 2
};

const MOUSE_STATE = {
    NONE: -1,
    ROTATE: 0,
    ZOOM: 1,
    PAN: 2,
    TOUCH_ROTATE: 3,
    TOUCH_ZOOM_PAN: 4
};

const HANDLER_TYPE = {
    NONE: -1,
    MODEL: 0,
    CLIPPING: 1
};

const HANDLE_MOUSE_STATE = {
    NONE: -1,
    OVER: 0,
    DOWN: 1
};

const RENDER_PRIORITY = {
    DISTANCE: 0,
    LOD: 1,
    SHUFFLE : 2
};

const VIEWCUBE_POSITIONS = {
    LEFT_TOP: 0,
    RIGHT_TOP: 1,
    LEFT_BOTTOM: 2,
    RIGHT_BOTTOM: 3
};

const CONFIG_KEY = {
    RENDER : {
        PROGRESSIVE: {
            ENABLE: "Configuration.Render.Progressive.Enable",
            LIMITCOUNT: "Configuration.Render.Progressive.LimitCount",
        },
        CACHE: {
            ENABLE: "Configuration.Render.Cache.Enable",
            LIMITCOUNT: "Configuration.Render.Cache.LimitCount",
        },
        PRIORITY : "Configuration.Render.Priority"
    }
};