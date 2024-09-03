package org.minohara

import kotlin.math.*

class WayPoint(val x: Double, val y: Double, val z: Double)

enum class State {
    IDLE,
    UP,
    DOWN,
    RIGHT,
    LEFT,
    FORWARD,
    FINISHED
}

class Controller(sim:Simulator) {
    val waypoints = listOf(
//        WayPoint(10.0, -10.0, 3.0),
//        WayPoint(-10.0, 10.0, 3.0),
//        WayPoint(0.0, 0.0, 1.2)

        WayPoint(0.0, -6.0, 4.0 ),
        WayPoint(8.0, -6.0, 4.0 ),
        WayPoint(8.0,  6.0, 4.0 ),
        WayPoint(-8.0, 6.0, 4.0 ),
        WayPoint(-8.0, -6.0, 4.0 ),
        WayPoint(0.0, -6.0, 4.0 ),
        WayPoint(0.0, 0.0, 1.2 ),
    )
    var state = State.IDLE
    var wpindex = 0

    var x = 0.0
    var y = 0.0
    var z = 0.0
    var yaw = 0.0
    var distance = 1000.0

    var sim:Simulator
    init {
        this.sim = sim
        sim.attitudeListener = ::attitudeListener
        sim.locationListener = ::locationListener
    }
    fun distance(x1: Double, x2: Double, y1: Double, y2: Double) : Double {
        return sqrt((x1-x2).pow(2) + (y1-y2).pow(2))
    }
    fun angle(x1: Double, x2: Double, y1: Double, y2: Double) : Double {
        return atan2(x1-x2, y1-y2)
    }
    fun control(x:Double, y:Double, z:Double, yaw: Double) : Boolean {
        //println("control (%f, %f, %f, %f) %s %f".format(x, y, z, yaw * 180 / PI, state, distance))
        println(" %s %.1f".format(state, distance))
        val ox = waypoints[wpindex].x
        val oy = waypoints[wpindex].y
        val oz = waypoints[wpindex].z
        if ( distance(ox, x, oy, y) < 0.4 && abs(oz - z) < 0.1 ) {
            println("=====WAYPOINT (%d, %.1f, %.1f, %.1f)=============".format(wpindex+1, x, y, z))
            wpindex++
            if (wpindex >= waypoints.size) {
                state = State.FINISHED
                return true
            }
            else {
                state = State.IDLE
            }
            sim.vslv( 0.0 )
            sim.vslh( 0.0 )
            sim.vsrv( 0.0 )
        }
        if (state == State.UP) {
            //println("oz:$oz, z:$z")
            if ( oz - z < 0.1) {
                state = State.IDLE
                sim.vsrv( 0.0 )
            }
        }
        if (state == State.DOWN) {
            if ( oz - z > -0.1) {
                state = State.IDLE
                sim.vsrv( 0.0 )
            }
        }
        if (state == State.RIGHT) {
            //println("R (%f, %f)\n".format(atan2(waypoints[wpindex].x - x, waypoints[wpindex].y - y),yaw))

            if ( abs(angle(ox,x,oy,y)  - yaw) <= PI/48/2) {
                state = State.FORWARD
                sim.vslh( 0.0 )
                sim.vslv( 0.5 )
            }
        }
        if (state == State.LEFT) {
            //println("L (%f, %f)".format(atan2(waypoints[wpindex].x - x, waypoints[wpindex].y - y),yaw))
            if ( abs( yaw - angle(ox,x,oy,y)) <= PI/48/2 ) {
                state = State.FORWARD
                sim.vslh( 0.0 )
                sim.vslv( 0.5)
            }
        }
        if (state == State.FORWARD) {
            if (distance(ox, x, oy, y) > this.distance) {
                var dtheta = angle(ox,x,oy,y) - yaw
                if (dtheta <= -PI) dtheta += 2 * PI
                if (0 <= dtheta && dtheta <= PI) {
                    state = State.RIGHT
                    sim.vslh(PI/48)
                }
                else {
                    state = State.LEFT
                    sim.vslh(-PI/48)
                }
                sim.vslv(0.0)
            }
            else if ( distance(ox, x, oy, y) < 0.3) {
                state = State.DOWN
                sim.vslv( 0.0 )
                sim.vslh( 0.0 )
                sim.vsrv( -0.2 )
            }
        }
        if (state == State.IDLE) {
            if (waypoints[wpindex].z > z + 0.2) {
                state = State.UP
                sim.vsrv( 0.2 )
            }
            else if (distance(waypoints[wpindex].x, x, waypoints[wpindex].y, y) > 0.2 ) {
                var dtheta = angle(waypoints[wpindex].x,x,waypoints[wpindex].y,y) - yaw
                if (dtheta <= -PI) dtheta += 2 * PI
                println(dtheta)
                if (0 <= dtheta && dtheta <= PI) {
                    state = State.RIGHT
                    sim.vslh(PI/48)
                }
                else {
                    state = State.LEFT
                    sim.vslh(-PI/48)
                }
            }
            else if ( waypoints[wpindex].z < z - 0.2 ) {
                state = State.DOWN
                sim.vsrv( - 0.2 )
            }
        }
        if (state != State.FINISHED) {
            this.distance = distance(waypoints[wpindex].x, x, waypoints[wpindex].y, y)
        }
        return false
    }
    fun attitudeListener(pitch:Double, roll:Double, yaw:Double) : Boolean {
        this.yaw = yaw
        return control(this.x, this.y, this.z, this.yaw)
    }
    fun locationListener(x:Double, y:Double, z:Double) : Boolean {
        this.x = x
        this.y = y
        this.z = z
        return control(this.x, this.y, this.z, this.yaw)
    }
    fun start() {
        if (state == State.IDLE) {
            if (waypoints[wpindex].z > 1.2) {
                state = State.UP
                sim.vsrv(0.2)
            }
        }
    }
}