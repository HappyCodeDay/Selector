package uk.co.abp24

import com.leapmotion.leap._

import scala.collection.JavaConversions._

object Launch {
  val version = "0.1a";

  def main(args: Array[String]) {
    println("Input Selector V" + version)

    val listener: SampleListener = new SampleListener
    val controller = new Controller

    // Have the sample listener receive events from the controller
    controller.addListener(listener)

    while (true) Thread sleep (10000)

    controller.removeListener(listener)
  }
}

class SampleListener extends Listener {
  override def onInit(controller: Controller) {
    System.out.println("Initialized")
  }

  override def onConnect(controller: Controller) {
    System.out.println("Connected")
    controller.enableGesture(Gesture.Type.TYPE_SWIPE)
    controller.enableGesture(Gesture.Type.TYPE_CIRCLE)
    controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP)
    controller.enableGesture(Gesture.Type.TYPE_KEY_TAP)
  }

  override def onDisconnect(controller: Controller) {
    System.out.println("Disconnected")
  }

  override def onExit(controller: Controller) {
    System.out.println("Exited")
  }

  override def onFrame(controller: Controller) {
    val frame = controller.frame
    //System.out.println("Frame id: " + frame.id + ", timestamp: " + frame.timestamp + ", hands: " + frame.hands.count + ", fingers: " + frame.fingers.count + ", tools: " + frame.tools.count + ", gestures " + frame.gestures.count)
    if (!frame.hands.isEmpty) {
      val hand = frame.hands.get(0)
      val fingers = hand.fingers
      if (!fingers.isEmpty) {
        var avgPos = Vector.zero
        for (finger <- fingers) {
          avgPos = avgPos.plus(finger.tipPosition)
        }
        avgPos = avgPos.divide(fingers.count)
        //System.out.println("Hand has " + fingers.count + " fingers, average finger tip position: " + avgPos)
      }
      //System.out.println("Hand sphere radius: " + hand.sphereRadius + " mm, palm position: " + hand.palmPosition)
      val normal = hand.palmNormal
      val direction = hand.direction
      //System.out.println("Hand pitch: " + Math.toDegrees(direction.pitch) + " degrees, " + "roll: " + Math.toDegrees(normal.roll) + " degrees, " + "yaw: " + Math.toDegrees(direction.yaw) + " degrees")
    }

    val gestures: GestureList = frame.gestures
    for (g <- gestures) {
      g.`type` match {
        case Gesture.Type.TYPE_CIRCLE =>
          val circle = new CircleGesture(g)
          // Calculate clock direction using the angle between circle normal and pointable
          val clockwiseness = if (circle.pointable.direction.angleTo(circle.normal) <= Math.PI / 4) "clockwise" else "counterclockwise"
          // Calculate angle swept since last frame
          var sweptAngle: Double = 0
          if (circle.state != Gesture.State.STATE_START) {
            val previousUpdate = new CircleGesture(controller.frame(1).gesture(circle.id))
            sweptAngle = (circle.progress - previousUpdate.progress) * 2 * Math.PI
          }
          System.out.println("Circle id: " + circle.id + ", " + circle.state + ", progress: " + circle.progress + ", radius: " + circle.radius + ", angle: " + Math.toDegrees(sweptAngle) + ", " + clockwiseness)
        case Gesture.Type.TYPE_SWIPE =>
          val swipe = new SwipeGesture(g)
          System.out.println("Swipe id: " + swipe.id + ", " + swipe.state + ", position: " + swipe.position + ", direction: " + swipe.direction + ", speed: " + swipe.speed)
        case Gesture.Type.TYPE_SCREEN_TAP =>
          val screenTap = new ScreenTapGesture(g)
          System.out.println("Screen Tap id: " + screenTap.id + ", " + screenTap.state + ", position: " + screenTap.position + ", direction: " + screenTap.direction)
        case Gesture.Type.TYPE_KEY_TAP =>
          val keyTap = new KeyTapGesture(g)
          println("Key Tap id: " + keyTap.id + ", " + keyTap.state + ", position: " + keyTap.position + ", direction: " + keyTap.direction)
        case _ => System.out.println("Other gesture")
      }
    }
  }
}


