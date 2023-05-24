// Simple example of using NatNetClient.kt library
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
                "id: %2d, position: %2.2f, %2.2f, %2.2f, rotation: %2.2f, %2.2f, %2.2f".format(
                    id, pos[0], pos[1], pos[2], rot[0], rot[1], rot[2]
                )
            )
        }
        streamingClient.run()
    }
    natNetClientThread.start()
}