package org.minohara

fun main() {
    val sim = Simulator()
    val ctrl = Controller(sim)
    sim.takeoff()
    ctrl.start()
    sim.run()
    sim.landing()
}