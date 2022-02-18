import React, {useEffect} from 'react'
import './App.css'
import {BACKEND_HOST} from './api/endpoints'
import downloadFile from './utils/downloadFile'

let connection: WebSocket | null = null
let imageCapture: {
    takePhoto: () => Promise<Blob>
} | null = null
const configuration = {
    iceServers: [
        {
            urls: ['stun:stun1.l.google.com:19302', 'stun:stun2.l.google.com:19302']
        }
    ],
    iceCandidatePoolSize: 10
}

let peerConnection: RTCPeerConnection
let localStream: MediaStream
let remoteStream: MediaStream
// let roomId = null;

function App() {
    const [roomId, setRoomId] = React.useState('')
    const localVideoRef = React.useRef<HTMLVideoElement>(null)
    const remoteVideoRef = React.useRef<HTMLVideoElement>(null)
    // const fileRef = React.useRef<HTMLInputElement>(null)
    const [h, setH] = React.useState(false)
    useEffect(() => {
        const getHealth = async () => {
            try {
                const ans = await fetch(`https://${BACKEND_HOST}/health`)
                setH(ans.ok)
            } catch (e) {
                console.error(`health check failed::${e}`)
            }
        }
        if (!connection && !h) {
            getHealth()
        }
        if (!connection && h) {
            try {
                connection = new WebSocket(`wss://${BACKEND_HOST}/socket`)
                connection.binaryType = 'arraybuffer'
                connection.onopen = function () {
                    console.log('Connected to the signaling server')
                    // initialize(connection!);
                }
            } catch (e) {
                console.log('socket failed', e)
            }
        }
        if (connection && h) {
            connection.onmessage = (ev: MessageEvent<any>) => {
                if (ev.data instanceof ArrayBuffer) {
                    const bl = new Blob([ev.data])
                    downloadFile(bl, 'fromPartner.jpeg')
                }
                const messageObj = JSON.parse(ev.data)
                // Creating room below
                if (messageObj.event === 'answer' && !peerConnection.currentRemoteDescription && messageObj.answer) {
                    // Listening for remote session description below
                    const handler = async () => {
                        console.log('Got remote description: ', messageObj.answer)
                        const rtcSessionDescription = new RTCSessionDescription(messageObj.answer)
                        await peerConnection.setRemoteDescription(rtcSessionDescription)
                    }
                    handler()
                    // Listening for remote session description above

                    // Listen for remote ICE candidates below
                    // todo ICE
                    // Listen for remote ICE candidates above
                }
                // Creating room above

                // Joining room below
                if (messageObj.event === 'findRoomAnswer' && messageObj.success) {
                    const handler = async () => {
                        peerConnection = new RTCPeerConnection(configuration)
                        registerPeerConnectionListeners()
                        localStream.getTracks().forEach(track => {
                            peerConnection.addTrack(track, localStream)
                        })
                        // Code for collecting ICE candidates below
                        peerConnection.addEventListener('icecandidate', async event => {
                            if (!event.candidate) {
                                console.log('Got final candidate!')
                                return
                            }
                            console.log('Got candidate: ', event.candidate)
                            // callerCandidatesCollection.add(event.candidate.toJSON());
                            await connection?.send(
                                JSON.stringify({event: 'addCalleeCandidate', candidate: event.candidate.toJSON(), roomId})
                            )
                        })
                        // Code for collecting ICE candidates above

                        peerConnection.addEventListener('track', event => {
                            console.log('Got remote track:', event.streams[0])
                            event.streams[0].getTracks().forEach(track => {
                                console.log('Add a track to the remoteStream:', track)
                                remoteStream.addTrack(track)
                            })
                        })
                        const offer = messageObj.offer
                        console.log('Got offer:', offer)
                        await peerConnection.setRemoteDescription(new RTCSessionDescription(offer))
                        const answer = await peerConnection.createAnswer()
                        console.log('Created answer:', answer)
                        await peerConnection.setLocalDescription(answer)
                        const roomWithAnswer = {
                            answer: {
                                type: answer.type,
                                sdp: answer.sdp
                            }
                        }
                        connection?.send(JSON.stringify({event: 'roomWithAnswer', roomId, roomWithAnswer: roomWithAnswer.answer}))
                    }

                    handler()
                }
                // Joining room above

                // Listening for remote ICE candidates below
                if (messageObj.event === 'remoteCandidateAdded') {
                    console.log(`Got new remote ICE candidate: ${JSON.stringify(messageObj.candidate)}`)
                    const handler = async () => {
                        await peerConnection.addIceCandidate(new RTCIceCandidate(messageObj.candidate))
                    }
                    handler()
                }
                // Listening for remote ICE candidates above
            }
        }
        // todo ?
        // return () => {
        //     if (connection) {
        //         connection.close()
        //     }
        // }
    }, [h, roomId])
    // const handleCreateOffer = () => {
    //     peerConnection.createOffer().createOffer(function(offer) {
    //         send({
    //             event : "offer",
    //             data : offer
    //         });
    //         peerConnection.setLocalDescription(offer);
    //     }, function(error) {
    //         alert("Error creating an offer");
    //     });
    // }
    const handleOpenCameraAndMicro = React.useCallback(async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: false})
            if (localVideoRef.current) {
                localVideoRef.current.srcObject = stream
            }
            localStream = stream
            remoteStream = new MediaStream()
            if (remoteVideoRef.current) {
                remoteVideoRef.current.srcObject = remoteStream
            }
            const track = stream.getVideoTracks()[0]
            // @ts-ignore
            imageCapture = new ImageCapture(track)
        } catch (e) {
            console.error(`navigator.mediaDevices.getUserMedia:::${e}`)
        }
    }, [])
    const handleCreateRoom = React.useCallback(async () => {
        if (roomId) {
            await connection?.send(JSON.stringify({event: 'createRoom', roomId: roomId})) // todo ?
            peerConnection = new RTCPeerConnection(configuration)
            registerPeerConnectionListeners()
            localStream.getTracks().forEach(track => {
                peerConnection.addTrack(track, localStream)
            })

            // Code for collecting ICE candidates below
            peerConnection.addEventListener('icecandidate', async event => {
                if (!event.candidate) {
                    console.log('Got final candidate!')
                    // test!!!
                    if (remoteVideoRef.current) {
                        remoteVideoRef.current.srcObject = remoteStream
                    }
                    return
                }
                console.log('Got candidate: ', event.candidate)
                // callerCandidatesCollection.add(event.candidate.toJSON());
                await connection?.send(JSON.stringify({event: 'addCallerCandidate', candidate: event.candidate.toJSON(), roomId}))
            })
            // Code for collecting ICE candidates above

            // Code for creating a room below
            const offer = await peerConnection.createOffer()
            await peerConnection.setLocalDescription(offer)
            console.log('Created offer:', offer)

            const roomWithOffer = {
                offer: {
                    type: offer.type,
                    sdp: offer.sdp
                }
            }
            await connection?.send(JSON.stringify({event: 'addOffer', roomId: roomId, offer: roomWithOffer.offer})) // todo ?
            // Code for creating a room above

            peerConnection.addEventListener('track', event => {
                console.log('Got remote track:', event.streams[0])
                event.streams[0].getTracks().forEach(track => {
                    console.log('Add a track to the remoteStream:', track)
                    remoteStream.addTrack(track)
                })
            })
        }
    }, [roomId])
    const handleJoinRoom = React.useCallback(() => {
        if (roomId) {
            connection?.send(JSON.stringify({event: 'findRoom', roomId: roomId}))
        }
    }, [roomId])
    const handleHangUp = React.useCallback(() => {
        const tracks = (localVideoRef.current?.srcObject as MediaStream)?.getTracks()
        tracks.forEach(track => {
            track.stop()
        })
        if (remoteStream) {
            remoteStream.getTracks().forEach(track => track.stop())
        }
        if (peerConnection) {
            peerConnection.close()
        }
        // Delete room on hangup
        // delete calleeCandidates and callerCandidates
        // todo
    }, [])

    const handleTakePhoto = React.useCallback(async () => {
        if (imageCapture) {
            const photoBlob = await imageCapture.takePhoto()
            downloadFile(photoBlob, 'snapshot.jpeg')
        }
    }, [])

    const handleSendPhoto = React.useCallback(() => {
        if (imageCapture) {
            imageCapture
                .takePhoto()
                .then(photoBlob => photoBlob.arrayBuffer())
                .then(arBuf => {
                    console.log('arBuf', arBuf)
                    connection?.send(arBuf)
                })
                // .then(photoBlob => {
                //     connection?.send(photoBlob)
                //     return photoBlob
                // })
                .then(photoBlob => {
                    console.log('photo is sent', photoBlob)
                })
                .catch(e => console.error(`send photo failed:::${e}`))
            // const photoBlob = await imageCapture.takePhoto()
            // await connection.send(photoBlob)
            // console.log('photo is sent')
        }
    }, [])

    const handleSendFile = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        const {files} = e.target
        if (files?.length) {
            const file = files[0]
            file.arrayBuffer()
                .then(b => connection?.send(b))
                .then(() => console.log('file is sent'))
                .catch(e => console.error(`send file failed:::${e}`))
        }
    }, [])

    return (
        <div className="App">
            <input
                onChange={v => {
                    setRoomId(v.target.value)
                }}
                value={roomId}
                placeholder="Room id"
            />
            <button onClick={handleOpenCameraAndMicro}>Open camera & microphone</button>
            <button onClick={handleCreateRoom}>Create room</button>
            <button onClick={handleJoinRoom}>Join room</button>
            <button onClick={handleHangUp}>Hang up</button>
            <button onClick={handleTakePhoto}>Take photo</button>
            <button onClick={handleSendPhoto}>Send photo</button>
            <div>
                <video ref={localVideoRef} id="localVideo" muted autoPlay playsInline />
                <video ref={remoteVideoRef} id="remoteVideo" autoPlay playsInline />
            </div>
            <input
                // ref={fileRef}
                type="file"
                onChange={handleSendFile}
            />
        </div>
    )
}

export default App

function registerPeerConnectionListeners() {
    peerConnection.addEventListener('icegatheringstatechange', () => {
        console.log(`ICE gathering state changed: ${peerConnection.iceGatheringState}`)
    })

    peerConnection.addEventListener('connectionstatechange', () => {
        console.log(`Connection state change: ${peerConnection.connectionState}`)
    })

    peerConnection.addEventListener('signalingstatechange', () => {
        console.log(`Signaling state change: ${peerConnection.signalingState}`)
    })

    peerConnection.addEventListener('iceconnectionstatechange ', () => {
        console.log(`ICE connection state change: ${peerConnection.iceConnectionState}`)
    })
}
