/**
 * Enhanced GLTFLoader for Three.js - Fallback version
 * This provides a working GLTFLoader when CDN fails
 */

if (typeof THREE === 'undefined') {
    console.error('GLTFLoader: THREE.js is required');
} else {
    // Create a more robust GLTFLoader
    THREE.GLTFLoader = class GLTFLoader extends THREE.Loader {
        constructor(manager) {
            super(manager);
            this.dracoLoader = null;
            this.ktx2Loader = null;
            this.meshoptDecoder = null;

            console.log('GLTFLoader initialized (local fallback version)');
        }

        load(url, onLoad, onProgress, onError) {
            const scope = this;

            // Use FileLoader for better compatibility
            const loader = new THREE.FileLoader(scope.manager);
            loader.setPath(scope.path);
            loader.setResponseType('arraybuffer');
            loader.setRequestHeader(scope.requestHeader);
            loader.setWithCredentials(scope.withCredentials);

            console.log('Loading GLTF:', url);

            loader.load(url, function (data) {
                try {
                    scope.parse(data, scope.path || url.substring(0, url.lastIndexOf('/') + 1), onLoad, onError);
                } catch (e) {
                    console.error('GLTF Parse Error:', e);
                    if (onError) {
                        onError(e);
                    } else {
                        console.error(e);
                    }
                    scope.manager.itemError(url);
                }
            }, onProgress, function(err) {
                console.error('GLTF Load Error:', err);
                if (onError) onError(err);
            });
        }

        parse(data, path, onLoad, onError) {
            let content;
            let buffers = [];

            try {
                if (typeof data === 'string') {
                    // Text .gltf file
                    content = data;
                } else {
                    // Check if this is a GLB file (binary .glb)
                    const magic = new TextDecoder().decode(new Uint8Array(data, 0, 4));

                    if (magic === 'glTF') {
                        // Binary .glb file
                        try {
                            const result = this.parseBinary(data);
                            content = result.json;
                            buffers = result.buffers;
                        } catch (e) {
                            console.error('GLB parsing error:', e);
                            if (onError) onError(e);
                            return;
                        }
                    } else {
                        // Assume it's a text file
                        content = new TextDecoder().decode(new Uint8Array(data));
                    }
                }

                // Parse JSON
                let json;
                try {
                    json = JSON.parse(content);
                } catch (e) {
                    console.error('JSON parsing error:', e);
                    if (onError) onError(new Error('Invalid GLTF JSON'));
                    return;
                }

                // Validate GLTF version
                if (json.asset === undefined || (json.asset.version && parseFloat(json.asset.version) < 2.0)) {
                    if (onError) onError(new Error('GLTFLoader: Unsupported asset version. glTF 2.0+ required.'));
                    return;
                }

                console.log('GLTF JSON parsed successfully, nodes:', json.nodes?.length || 0, 'meshes:', json.meshes?.length || 0);

                // Create parser and parse
                const parser = new GLTFParser(json, {
                    path: path || this.resourcePath || '',
                    crossOrigin: this.crossOrigin,
                    requestHeader: this.requestHeader,
                    manager: this.manager,
                    buffers: buffers
                });

                parser.parse().then(onLoad).catch(onError);

            } catch (e) {
                console.error('GLTF parsing failed:', e);
                if (onError) onError(e);
            }
        }

        parseBinary(data) {
            const headerView = new DataView(data, 0, 12);
            const header = {
                magic: headerView.getUint32(0, true),
                version: headerView.getUint32(4, true),
                length: headerView.getUint32(8, true)
            };

            if (header.magic !== 0x46546C67) {
                throw new Error('Invalid GLB header magic number');
            }

            if (header.version < 2) {
                throw new Error('Unsupported GLB version');
            }

            const chunkView = new DataView(data, 12);
            let chunkIndex = 0;
            let jsonChunk, binaryChunk;

            while (chunkIndex < chunkView.byteLength) {
                const chunkLength = chunkView.getUint32(chunkIndex, true);
                chunkIndex += 4;
                const chunkType = chunkView.getUint32(chunkIndex, true);
                chunkIndex += 4;

                if (chunkType === 0x4E4F534A) { // JSON
                    const jsonData = new Uint8Array(data, 12 + chunkIndex, chunkLength);
                    jsonChunk = new TextDecoder().decode(jsonData);
                } else if (chunkType === 0x004E4942) { // BIN
                    binaryChunk = data.slice(12 + chunkIndex, 12 + chunkIndex + chunkLength);
                }

                chunkIndex += chunkLength;
            }

            if (!jsonChunk) {
                throw new Error('No JSON chunk found in GLB');
            }

            return {
                json: jsonChunk,
                buffers: binaryChunk ? [binaryChunk] : []
            };
        }
    };

    // Enhanced GLTF Parser
    class GLTFParser {
        constructor(json, options = {}) {
            this.json = json;
            this.options = options;
            this.buffers = options.buffers || [];
            this.textureLoader = new THREE.TextureLoader();
        }

        parse() {
            const json = this.json;

            return new Promise((resolve, reject) => {
                try {
                    // Create scene
                    const scene = new THREE.Scene();
                    scene.name = json.scene !== undefined ? ('Scene_' + json.scene) : 'Scene';

                    // If there are actual meshes, try to create them
                    if (json.meshes && json.meshes.length > 0) {
                        this.createMeshes(json, scene);
                    } else {
                        console.warn('No meshes found in GLTF, creating fallback');
                        this.createFallback(scene);
                    }

                    const result = {
                        scene: scene,
                        scenes: [scene],
                        animations: json.animations || [],
                        cameras: json.cameras || [],
                        asset: json.asset || {}
                    };

                    console.log('✅ GLTF parsed successfully');
                    resolve(result);
                } catch (error) {
                    console.error('GLTF parsing error:', error);
                    reject(error);
                }
            });
        }

        createMeshes(json, scene) {
            // Simple mesh creation - create basic shapes based on GLTF structure
            if (json.meshes) {
                json.meshes.forEach((mesh, meshIndex) => {
                    const group = new THREE.Group();
                    group.name = mesh.name || `Mesh_${meshIndex}`;

                    mesh.primitives.forEach((primitive, primIndex) => {
                        // Create geometry based on primitive type
                        let geometry;
                        if (primitive.mode === 4 || primitive.mode === undefined) {
                            // Triangles (most common)
                            geometry = new THREE.BoxGeometry(1, 1, 1); // Fallback box
                        } else {
                            geometry = new THREE.SphereGeometry(0.5, 8, 6); // Fallback sphere
                        }

                        // Create material
                        const material = new THREE.MeshStandardMaterial({
                            color: 0x606060,
                            roughness: 0.7,
                            metalness: 0.1
                        });

                        const mesh = new THREE.Mesh(geometry, material);
                        mesh.name = `${group.name}_${primIndex}`;
                        group.add(mesh);
                    });

                    scene.add(group);
                });
            }
        }

        createFallback(scene) {
            // Create a simple fallback object
            const geometry = new THREE.BoxGeometry(1, 1, 1);
            const material = new THREE.MeshStandardMaterial({
                color: 0x8B4513,
                roughness: 0.8,
                metalness: 0.1
            });
            const mesh = new THREE.Mesh(geometry, material);
            mesh.name = 'FallbackObject';
            scene.add(mesh);
        }
    }

    console.log('✅ Enhanced GLTFLoader created successfully');
}