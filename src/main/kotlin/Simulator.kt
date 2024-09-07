package org.minohara

import java.awt.*
import java.lang.Thread.sleep
import java.awt.geom.Line2D
import javax.sound.sampled.Line
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.*

class Simulator : JFrame("Simulator") {
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
    var lines : MutableList<Line2D> = mutableListOf()
    var drone : Polygon = Polygon()
    init {
        contentPane = JPanel()
        this.setSize(800,600)
        this.preferredSize = Dimension(800, 600)
        this.isVisible = true
    }
    override fun paint(g: Graphics?) {
        super.paint(g)
        val g2d = g as Graphics2D
        for (line in lines) {
            g2d.draw(line)
        }
        g2d.color = Color.RED
        g2d.fill(drone)
    }
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
    fun addLine(x1:Double, y1:Double, x2:Double, y2:Double) {
        lines.add(Line2D.Double(
            x1 * 10 + 400,
            -y1 * 10 + 300,
            x2 * 10 + 400,
            -y2 * 10 + 300
        ))
    }
    fun moveDrone(x:Double, y: Double, theta: Double) {
        val d = 10.0
        val cx = x * 10 + 400;
        val cy = -y * 10 + 300;
        drone.reset()
        drone.addPoint((cx+d*sin(theta)).toInt(), (cy-d*cos(theta)).toInt())
        drone.addPoint((cx+0.7*d*sin(theta+2.0*PI/3.0)).toInt(), (cy-0.7*d*cos(theta+2.0*PI/3.0)).toInt())
        drone.addPoint((cx+0.7*d*sin(theta-2.0*PI/3.0)).toInt(), (cy-0.7*d*cos(theta-2.0*PI/3.0)).toInt())
    }
    fun run () {
        while (t < 400) {
            var ox = x
            var oy = y
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
            addLine(ox, oy, x, y)
            moveDrone(x, y, theta)
            repaint((x * 10 + 400).toInt()-15,(-y * 10 + 300).toInt()-15,30,30)
            sleep(interval)
        }
    }
}