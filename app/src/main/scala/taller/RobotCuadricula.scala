package taller

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 *
 * Tal como está, el proyecto compila y las pruebas quedan en rojo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
    case (Norte, Derecha)   => Este
    case (Este,  Derecha)   => Sur
    case (Sur,   Derecha)   => Oeste
    case (Oeste, Derecha)   => Norte
    case (Norte, Izquierda) => Oeste
    case (Oeste, Izquierda) => Sur
    case (Sur,   Izquierda) => Este
    case (Este,  Izquierda) => Norte
  }

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(e: Estado, o: Orden): Estado = o match {
    case Avanzar(n) => e.mira match {
      case Norte => e.copy(y = e.y + n)
      case Sur   => e.copy(y = e.y - n)
      case Este  => e.copy(x = e.x + n)
      case Oeste => e.copy(x = e.x - n)
    }
    case Girar(s) => e.copy(mira = girar(e.mira, s))
    case Repetir(veces, _) if veces <= 0 => e
    case Repetir(veces, orden) =>
      ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
    case Secuencia(ordenes) =>
      ordenes.foldLeft(e)((acc, ord) => ejecutar(acc, ord))
  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int = o match {
    case Avanzar(_) => 1
    case Girar(_) => 1
    case Repetir(veces, orden) =>
      if (veces <= 0) 0 else veces * cuantasBasicas(orden)
    case Secuencia(ordenes) =>
      @annotation.tailrec
      def suma(pendientes: List[Orden], acum: Int): Int = pendientes match {
        case Nil => acum
        case h :: t => suma(t, acum + cuantasBasicas(h))
      }
      suma(ordenes, 0)
  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
  def profundidad(o: Orden): Int = o match {
    case Avanzar(_) => 1
    case Girar(_) => 1
    case Repetir(_, orden) => 1 + profundidad(orden)
    case Secuencia(ordenes) =>
      @annotation.tailrec
      def maximo(pendientes: List[Orden], mejor: Int): Int = pendientes match {
        case Nil => mejor
        case h :: t =>
          val ph = profundidad(h)
          maximo(t, if (ph > mejor) ph else mejor)
      }
      1 + maximo(ordenes, 0)
  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1, o2))

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden = {
    val paso: Orden = Secuencia(List(Avanzar(pasos), Girar(giro)))
    Repetir(veces, paso)
  }

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] = {
    @annotation.tailrec
    def recorre(pendientes: List[Estado], acum: List[Estado]): List[Estado] =
      pendientes match {
        case Nil => acum.reverse
        case h :: t => recorre(t, ejecutar(h, o) :: acum)
      }
    recorre(estados, Nil)
  }

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
  def transformar(o: Orden, f: Orden => Orden): Orden = o match {
    case basica @ Avanzar(_)      => f(basica)
    case basica @ Girar(_)        => f(basica)
    case Repetir(veces, orden)    => Repetir(veces, transformar(orden, f))
    case Secuencia(ordenes)       =>
      @annotation.tailrec
      def recorre(pendientes: List[Orden], acum: List[Orden]): List[Orden] =
        pendientes match {
          case Nil    => acum.reverse
          case h :: t => recorre(t, transformar(h, f) :: acum)
        }
      Secuencia(recorre(ordenes, Nil))
  }

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden ={

    // 1. Simplificación previa de subórdenes (Bottom-Up)
    val simplInterno: Orden = o match {
      case Repetir(veces, orden) =>
        Repetir(veces, simplificar(orden))

      case Secuencia(ordenes) =>
        // Simplifica cada suborden interna primero
        @annotation.tailrec
        def Mapear(pendientes: List[Orden], acum: List[Orden]): List[Orden] = pendientes match {
          case Nil    => acum.reverse
          case h :: t => Mapear(t, simplificar(h) :: acum)
        }
        val ordenesSimplificadas = Mapear(ordenes, Nil)

        // Auxiliar recursiva para fusionar Avanzar seguidos y omitir neutros
        def combinar(lista: List[Orden]): List[Orden] = lista match {
          case Nil => Nil
          case Secuencia(Nil) :: rest => combinar(rest)                           // Regla 5: omite neutros
          case Avanzar(0) :: rest     => combinar(rest)                           // Regla 1/5: Avanzar(0) no hace nada
          case Avanzar(a) :: Avanzar(b) :: rest => combinar(Avanzar(a + b) :: rest) // Regla 2: combina Avanzar seguidos
          case h :: rest => h :: combinar(rest)
        }

        Secuencia(combinar(ordenesSimplificadas))

      case otra => otra
    }

    // 2. Aplicación de las reglas sobre la estructura actual
    val pasoPuntoFijo: Orden = simplInterno match {
      // Regla 1: Avanzar(0) y Repetir(0, _) se convierten en Secuencia(Nil)
      case Avanzar(0)    => Secuencia(Nil)
      case Repetir(0, _) => Secuencia(Nil)

        // Regla 3: Repetir(1, orden) se desenvuelve como orden
      case Repetir(1, sub) => sub

        // Regla 4: Secuencia de un solo elemento se convierte en ese elemento
      case Secuencia(List(elem)) => elem

        // Regla 5: Quitar órdenes neutras Secuencia(Nil)
      case Secuencia(ordenes) =>
        @annotation.tailrec
        def filtrarNeutras(pendientes: List[Orden], acum: List[Orden]): List[Orden] = pendientes match {
          case Nil => acum.reverse
          case Secuencia(Nil) :: t => filtrarNeutras(t, acum)
          case h :: t => filtrarNeutras(t, h :: acum)
        }
        val sinNeutras = filtrarNeutras(ordenes, Nil)
        sinNeutras match {
          case Nil         => Secuencia(Nil)
          case List(unica) => unica // Aplica Regla 4 si al filtrar queda 1 sola
          case varias      => Secuencia(varias)
        }

      case otra => otra
    }

    // 3. Proceso hasta punto fijo: si cambió, se vuelve a simplificar
    if (pasoPuntoFijo == o) pasoPuntoFijo
    else simplificar(pasoPuntoFijo)
  }


}
