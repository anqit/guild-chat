package com.ankit.guild.chat.data.dao.util

import scala.concurrent.{ExecutionContext, Future}

// actually using an exercise solution!
object FunctionalUtils {

  def sequence[A](fs: List[Future[A]])(implicit ex: ExecutionContext): Future[List[A]] =
    fs match {
      case Nil => Future.successful(Nil)
      case x :: xs => x flatMap (xx => sequence(xs) map (xx :: _))
    }
}
