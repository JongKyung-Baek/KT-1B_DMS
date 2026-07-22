// REF: https://stackoverflow.com/a/47880734

import binding from './zstd-codec-binding-wasm.js';

const wasmSupported = (() => {
    try {
        if (typeof WebAssembly === "object"
            && typeof WebAssembly.instantiate === "function") {
            var module = new WebAssembly.Module(Uint8Array.of(0x0, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00));
            if (module instanceof WebAssembly.Module)
                return new WebAssembly.Instance(module) instanceof WebAssembly.Instance;
        }
    } catch (e) {
    }
    return false;
})();

class module {
    constructor(){
        this.run = (f) => {
            const Module = {};
            Module.onRuntimeInitialized = () => {
                f(Module);
            };
        
            const codec = new binding(Module);
            if (wasmSupported) {
                //require('./zstd-codec-binding-wasm.js')(Module);
            }
            else 
            {
                //require('./zstd-codec-binding.js')(Module);
            }
        };
    }
}

// exports.run = (f) => {
//     const Module = {};
//     Module.onRuntimeInitialized = () => {
//         f(Module);
//     };

//     if (wasmSupported) {
//         require('./zstd-codec-binding-wasm.js')(Module);
//     }
//     else 
//     {
//         require('./zstd-codec-binding.js')(Module);
//     }
// };

export {module}
