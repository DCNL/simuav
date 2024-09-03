package org.minohara

import java.lang.Thread.sleep
import kotlin.math.*

class Simulator {
    val interval:Long = 200
    var t = 0.0
    var dt = 1.0
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var theta = 0.0
    var omega = 0.0
    var v = 0.0
    var vh = 0.0
    var attitudeListener : ((pitch:Double, roll:Double, yaw:Double) -> Boolean) ? = null
    var locationListener : ((x:Double, y:Double, z:Double) -> Boolean) ? = null
    fun vslh(angle:Double) {
        omega = angle
    }
    fun vslv(angle:Double) {
        v = angle
    }
    fun vsrv(angle:Double) {
        println("--Up/Down--")
        vh = angle
    }
    fun takeoff() {
        println("--Takeoff--")
        if (z == 0.0) {
            for (i in 1..6) {
                t += dt
                z += 0.2
                println("t:%8.1f, x:%6.1f, y:%6.1f, z:%6.1f, θ:%6.1f".format(t, x, y, z, theta*180/ PI))
                sleep(interval)
            }
        }
    }
    fun landing() {
        println("--Landing--")
        while (z > 0.0) {
            t += dt
            z -= 0.2
            if (z < 0.0) z = 0.0
            println("t:%8.1f, x:%6.1f, y:%6.1f, z:%6.1f, θ:%6.1f".format(t, x, y, z, theta*180/ PI))
            sleep(interval)
        }
    }
    fun run () {
        while (t < 400) {
            t += dt
            theta += omega * dt
            if (theta > PI) theta -= 2 * PI
            else if (theta < -PI) theta += 2 * PI
            x += v * sin(theta) * dt
            y += v * cos(theta) * dt
            z += vh * dt
            if (abs(omega) > 0.0) {
                print("t:%8.1f, x:%6.1f, y:%6.1f, z:%6.1f, θ:%6.1f".format(t, x, y, z, theta*180/ PI))
                if (attitudeListener?.invoke(0.0, 0.0, theta)?:true)
                    break
            }
            if (abs(v) > 0.0 || abs(vh) > 0.0) {
                print("t:%8.1f, x:%6.1f, y:%6.1f, z:%6.1f, θ:%6.1f".format(t, x, y, z, theta*180/ PI))
                if (locationListener?.invoke(x, y, z)?:true)
                    break
            }
            sleep(interval)
        }
    }
}