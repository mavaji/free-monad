package monad

case class IO[A](unsafeRun: () => A)

object IO:
  def create[A](a: => A): IO[A] = IO(() => a)

given ioMonad: Monad[IO] with
  override def pure[A](a: A): IO[A] = IO(() => a)

  override def flatMap[A, B](ma: IO[A])(f: A => IO[B]): IO[B] = IO(() => f(ma.unsafeRun()).unsafeRun())
