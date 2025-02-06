package monad

trait ~>[F[_], G[_]]:
  def apply[A](fa: F[A]): G[A]

sealed trait Free[F[_], A]:
  def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap(this, f)

  import Free.pure

  def map[B](f: A => B): Free[F, B] = flatMap(a => pure(f(a)))

  def foldMap[G[_] : Monad](nt: F ~> G): G[A] =
    this match
      case Pure(a) => Monad[G].pure(a)
      case LiftF(fa) => nt.apply(fa)
      case FlatMap(fa, f) =>
        Monad[G].flatMap(fa.foldMap(nt))(a => f(a).foldMap(nt))

object Free:
  def pure[F[_], A](a: A): Free[F, A] = Pure(a)

  def liftF[F[_], A](fa: F[A]): Free[F, A] = LiftF(fa)

case class Pure[F[_], A](a: A) extends Free[F, A]

case class FlatMap[F[_], A, B](fa: Free[F, A], f: A => Free[F, B]) extends Free[F, B]

case class LiftF[F[_], A](fa: F[A]) extends Free[F, A]
