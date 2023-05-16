// OptiTrack NatNet direct depacketization sample for Kotlin
//
// Uses the Kotlin NatNetClient.kt library to establish a connection (by creating a NatNetClient),
// and receive data via a NatNet connection and decode it using the NatNetClient library.

import java.lang.Exception
import java.net.InetSocketAddress
import kotlin.system.exitProcess

// This is a callback function that gets connected to the NatNet client
// and called once per mocap frame.
fun receiveNewFrame(dataDict: MutableMap<String, Any>) {
    val orderList = arrayOf(
        "frameNumber", "markerSetCount", "unlabeledMarkersCount", "rigidBodyCount", "skeletonCount",
        "labeledMarkerCount", "timecode", "timecodeSub", "timestamp", "isRecording", "trackedModelsChanged"
    )
    val dumpArgs = false
    if (dumpArgs) {
        var outString = "    "
        for (key in dataDict.keys) {
            outString += "$key="
            if (key in dataDict) {
                outString += dataDict[key] as String + " "
            }
            outString += "/"
        }
        println(outString)
    }
}

// This is a callback function that gets connected to the NatNet client. It is called once per rigid body per frame
fun receiveRigidBodyFrame(newId: Int, position: ArrayList<Double>, rotation: ArrayList<Double>) {
    //
//    println("Received frame for rigid body$newId")
//    println("Received frame for rigid body$newId position rotation")
}

private fun addLists(totals: ArrayList<Int>, totalsTmp: ArrayList<Int>): ArrayList<Int> {
    totals[0] += totalsTmp[0]
    totals[1] += totalsTmp[1]
    totals[2] += totalsTmp[2]
    return totals
}

fun printConfiguration(natnetClient: NatNetClient) {
    println("Connection Configuration:")
    println("  Client:          %s".format(natnetClient.localIpAddress))
    println("  Server:          %s".format(natnetClient.serverIpAddress))
    println("  Command Port:    %d".format(natnetClient.commandPort))
    println("  Data Port:       %d".format(natnetClient.dataPort))

    if (natnetClient.useMulticast) {
        println("  Using Multicast")
        println("  Multicast Group: %s".format(natnetClient.multicastAddress))
    } else {
        println("  Using Unicast")
    }

//    NatNet Server Info
    val applicationName = natnetClient.getApplicationName()
    val natNetRequestedVersion = natnetClient.getNatNetRequestedVersion()
    val natNetVersionServer = natnetClient.getNatNetVersionServer()
    val serverVersion = natnetClient.getServerVersion()

    println("  NatNet Server Info")
    println("    Application Name %s".format(applicationName))
    println(
        "    NatNetVersion  %d %d %d %d".format(
            natNetVersionServer[0],
            natNetVersionServer[1],
            natNetVersionServer[2],
            natNetVersionServer[3]
        )
    )
    println(
        "    ServerVersion  %d %d %d %d".format(
            serverVersion[0],
            serverVersion[1],
            serverVersion[2],
            serverVersion[3]
        )
    )
    println("  NatNet Bitstream Requested")
    println(
        "    NatNetVersion  %d %d %d %d".format(
            natNetRequestedVersion[0], natNetRequestedVersion[1],
            natNetRequestedVersion[2], natNetRequestedVersion[3]
        )
    )
//    println("commandSocket = %s".format(natnetClient.commandSocket))
//    println("dataSocket    = %s".format(natnetClient.dataSocket))
}

fun printCommands(canChangeBitstream: Boolean) {
    var outstring = "Commands:\n"
    outstring += "Return Data from Motive\n"
    outstring += "  s  send data descriptions\n"
    outstring += "  r  resume/start frame playback\n"
    outstring += "  p  pause frame playback\n"
    outstring += "     pause may require several seconds\n"
    outstring += "     depending on the frame data size\n"
    outstring += "Change Working Range\n"
    outstring += "  o  reset Working Range to: start/current/end frame = 0/0/end of take\n"
    outstring += "  w  set Working Range to: start/current/end frame = 1/100/1500\n"
    outstring += "Return Data Display Modes\n"
    outstring += "  j  printLevel = 0 supress data description and mocap frame data\n"
    outstring += "  k  printLevel = 1 show data description and mocap frame data\n"
    outstring += "  l  printLevel = 20 show data description and every 20th mocap frame data\n"
    outstring += "Change NatNet data stream version (Unicast only)\n"
    outstring += "  3  Request 3.1 data stream (Unicast only)\n"
    outstring += "  4  Request 4.0 data stream (Unicast only)\n"
    outstring += "t  data structures test (no motive/server interaction)\n"
    outstring += "c  show configuration\n"
    outstring += "h  print commands\n"
    outstring += "q  quit\n"
    outstring += "\n"
    outstring += "NOTE: Motive frame playback will respond differently in\n"
    outstring += "       Endpoint, Loop, and Bounce playback modes.\n"
    outstring += "\n"
    outstring += "EXAMPLE: PacketClient [serverIP [ clientIP [ Multicast/Unicast]]]\n"
    outstring += "         PacketClient \"192.168.10.14\" \"192.168.10.14\" Multicast\n"
    outstring += "         PacketClient \"127.0.0.1\" \"127.0.0.1\" u\n"
    outstring += "\n"
    println(outstring)
}

fun requestDataDescriptions(sClient: NatNetClient) {
//    Request the model definitions
    sClient.sendRequest(
        sClient.commandSocket,
        sClient.NAT_REQUEST_MODELDEF,
        "",
        InetSocketAddress(sClient.serverIpAddress, sClient.commandPort)
    )
}

fun testClasses() {
    var totals = arrayListOf(0, 0, 0)
    println("Test Data Description Classes")
    var totalsTmp = testAllDataDescriptions()
    totals = addLists(totals, totalsTmp)
    println("")
    println("Test MoCap Frame Classes")
    totalsTmp = testAllMoCapData()
    totals = addLists(totals, totalsTmp)
    println("")
    println("All Tests totals")
    println("--------------------")
    println("[PASS] Count = %3d".format(totals[0]))
    println("[FAIL] Count = %3d".format(totals[1]))
    println("[SKIP] Count = %3d".format(totals[2]))
}

fun myParseArgs(argList: Array<String>, argsDict: MutableMap<String, Any>): MutableMap<String, Any> {
//    set up base values
    val argListLen = argList.size
    if (argListLen > 0) {
        argsDict["serverAddress"] = argList[0]
        if (argListLen > 1) {
            argsDict["clientAddress"] = argList[1]
        }
        if (argListLen > 2) {
            if (argList[2].length > 1) {
                argsDict["useMulticast"] = true
                if (argList[2][0].uppercase() == "U") {
                    argsDict["useMulticast"] = false
                }
            }
        }
    }

    return argsDict
}

fun main(args: Array<String>) {

    var optionsDict = mutableMapOf<String, Any>()
    optionsDict["clientAddress"] = "127.0.0.1"
    optionsDict["serverAddress"] = "192.168.208.20"
    optionsDict["useMulticast"] = true

    // This will create a new NatNet client
    optionsDict = myParseArgs(args, optionsDict)

    val streamingClient = NatNetClient()
    streamingClient.setClientAddress(optionsDict["clientAddress"] as String)
    streamingClient.setServerAddress(optionsDict["serverAddress"] as String)
    streamingClient.useMulticast = optionsDict["useMulticast"] as Boolean

    // Configure the streaming client to call our rigid body handler on the emulator to send data out
    streamingClient.newFrameListener = (object : NatNetClient.NewFrameListener {
        override fun onReceive(dataDict: MutableMap<String, Any>) {
            receiveNewFrame(dataDict)
        }
    })
    streamingClient.rigidBodyListener = (object : NatNetClient.RigidBodyListener {
        override fun onReceive(newId: Int, pos: ArrayList<Double>, rot: ArrayList<Double>) {
            receiveRigidBodyFrame(newId, pos, rot)
        }
    })

    // Start up the streaming client now that the callbacks are set up .
    // This will run perpetually, and operate on a separate thread.
    val isRunning = streamingClient.run()
    if (!isRunning) {
        println("ERROR: Could not start streaming client.")
        try {
            exitProcess(1)
        } catch (e: Exception) {
            println("...")
        } finally {
            println("exiting")
        }
    }

    Thread.sleep(1000)
    if (!streamingClient.connected()) {
        println("ERROR: Could not connect properly. Check that Motive streaming is on.")
        try {
//            exitProcess(2)
        } catch (e: Exception) {
            println("...")
        } finally {
            println("exiting")
        }
    }

    printConfiguration(streamingClient)
    println("\n")
    printCommands(streamingClient.canChangeBitstreamVersion())

    while (true) {
        print("Enter command or (\'h\' for list of commands)\n")
        val inchars = readlnOrNull()
        if (inchars!!.isNotEmpty()) {
            val c1 = inchars[0].lowercase()
            if (c1 == "h") {
                printCommands(streamingClient.canChangeBitstreamVersion())
            } else if (c1 == "c") {
                printConfiguration(streamingClient)
            } else if (c1 == "s") {
                requestDataDescriptions(streamingClient)
                Thread.sleep(1000)
            } else if ((c1 == "3") or (c1 == "4")) {
                if (streamingClient.canChangeBitstreamVersion()) {
                    var tmpMajor = 4
                    var tmpMinor = 0
                    if (c1 == "3") {
                        tmpMajor = 3
                        tmpMinor = 1
                    }
                    val returnCode = streamingClient.setNatNetVersion(tmpMajor, tmpMinor)
                    Thread.sleep(1000)
                    if (returnCode == -1) {
                        println("Could not change bitstream version to %d.%d".format(tmpMajor, tmpMinor))
                    } else {
                        println("Bitstream version at %d.%d".format(tmpMajor, tmpMinor))
                    }
                } else {
                    println("Can only change bitstream in Unicast Mode")
                }

            } else if (c1 == "p") {
                val szCommand = "TimelineStop"
                val returnCode = streamingClient.sendCommand(szCommand)
                Thread.sleep(1000)
                println("Command: %s - return_code: %d".format(szCommand, returnCode))
            } else if (c1 == "r") {
                val szCommand = "TimelinePlay"
                val returnCode = streamingClient.sendCommand(szCommand)
                println("Command: %s - return_code: %d".format(szCommand, returnCode))
            } else if (c1 == "o") {
                val tmpCommands = arrayListOf(
                    "TimelinePlay",
                    "TimelineStop",
                    "SetPlaybackStartFrame,0",
                    "SetPlaybackStopFrame,1000000",
                    "SetPlaybackLooping,0",
                    "SetPlaybackCurrentFrame,0",
                    "TimelineStop"
                )
                for (szCommand in tmpCommands) {
                    val returnCode = streamingClient.sendCommand(szCommand)
                    println("Command: %s - return_code: %d".format(szCommand, returnCode))
                }
                Thread.sleep(1000)
            } else if (c1 == "w") {
                val tmpCommands = arrayListOf(
                    "TimelinePlay",
                    "TimelineStop",
                    "SetPlaybackStartFrame,10",
                    "SetPlaybackStopFrame,1500",
                    "SetPlaybackLooping,0",
                    "SetPlaybackCurrentFrame,100",
                    "TimelineStop"
                )
                for (szCommand in tmpCommands) {
                    val returnCode = streamingClient.sendCommand(szCommand)
                    println("Command: %s - return_code: %d".format(szCommand, returnCode))
                }
                Thread.sleep(1000)
            } else if (c1 == "t") {
                testClasses()

            } else if (c1 == "j") {
                streamingClient.printLevel = 0
                println("Showing only received frame numbers and suppressing data descriptions")
            } else if (c1 == "k") {
                streamingClient.printLevel = 1
                println("Showing every received frame")

            } else if (c1 == "l") {
                streamingClient.printLevel = 20
                val printLevel = streamingClient.printLevel
                val printLevelMod = printLevel % 100
                if (printLevel == 0) {
                    println("Showing only received frame numbers and suppressing data descriptions")
                } else if ((printLevel == 1)) {
                    println("Showing every frame")
                } else if ((printLevelMod == 1)) {
                    println("Showing every %dst frame".format(printLevel))
                } else if ((printLevelMod == 2)) {
                    println("Showing every %dnd frame".format(printLevel))
                } else if ((printLevel == 3)) {
                    println("Showing every %drd frame".format(printLevel))
                } else {
                    println("Showing every %dth frame".format(printLevel))
                }

            } else if (c1 == "q") {
                streamingClient.shutdown()
                break
            } else {
                println("Error: Command %s not recognized".format(c1))
            }
            println("Ready...\n")
        }
    }
    println("exiting")
}
