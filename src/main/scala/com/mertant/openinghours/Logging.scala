package com.mertant.openinghours

import akka.actor.ActorSystem

trait Logging {
  val system: ActorSystem
  val log = system.log
}
