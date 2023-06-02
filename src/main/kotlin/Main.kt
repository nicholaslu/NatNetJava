// Simple example of using NatNetClient.kt library

var rigidBodyMap = mutableMapOf<Int, String>()

//val printRigidBodyFrameWithName = { newId: Int, position: ArrayList<Double>, rotation: ArrayList<Double> ->
//    val name = rigidBodyMap[newId]
//    println("id: $newId, name: $name ,pos: $position, rot: $rotation")
//}

//val receiveDataDescriptions = { dataDescs: DataDescriptions ->
//    dataDescs.rigidBodyList.forEach { rigidBodyMap[it.idNum] = it.szName }
//}

@OptIn(ExperimentalUnsignedTypes::class)
fun main() {
    val natNetClientThread = Thread {
        val streamingClient = NatNetClient()
        streamingClient.localIpAddress = "127.0.0.1"
        streamingClient.serverIpAddress = "192.168.208.20"
        streamingClient.useMulticast = true
        streamingClient.multicastAddress = "239.255.42.99"
        streamingClient.rigidBodyListener = { id: Int, pos: ArrayList<Double>, rot: ArrayList<Double> ->
            println(
                "id: %2d, name: %s, position: %2.2f, %2.2f, %2.2f, rotation: %2.2f, %2.2f, %2.2f".format(
                    id, rigidBodyMap[id], pos[0], pos[1], pos[2], rot[0], rot[1], rot[2]
                )
            )
        }
        streamingClient.dataDescriptionsListener = { dataDescs: DataDescriptions ->
            dataDescs.rigidBodyList.forEach { rigidBodyMap[it.idNum] = it.szName }
        }
        streamingClient.run()
    }
    natNetClientThread.start()
}