# Informe de corrección — Taller 2: un robot en la cuadrícula

Fundamentos de Programación Funcional y Concurrente.

Para cada función se define qué debe calcular (su especificación) con notación
matemática, y se demuestra que el programa en Scala la cumple. Como `Orden` es
un tipo definido recursivamente, las demostraciones sobre órdenes usan
inducción estructural, igual que en el ejemplo del factorial.

## Punto 1: `girar`

### Especificación

Se numeran las direcciones en el sentido de las agujas del reloj:

```math
\text{Norte} = 0,\quad \text{Este} = 1,\quad \text{Sur} = 2,\quad \text{Oeste} = 3
```

y se asigna a cada sentido un desplazamiento: $\delta(\text{Derecha}) = +1$ y
$\delta(\text{Izquierda}) = -1$. Girar noventa grados es avanzar $\delta(s)$
posiciones en ese ciclo de cuatro, así que la función que se quiere calcular es

```math
g(d, s) = (\,\text{idx}(d) + \delta(s)\,) \bmod 4
```

### Teorema

Para toda dirección $d$ y todo sentido $s$, `girar(d, s)` es la dirección con
índice $g(d, s)$.

### Demostración

`Direccion` y `Sentido` no son recursivos: tienen un número finito de valores
y no hace falta inducción. Basta con revisar los $4 \times 2 = 8$ casos, que
son exactamente los casos del `match`:

| $d$ | $s$ | $g(d, s)$ | Valor de `girar(d, s)` | ¿Coinciden? |
|---|---|---|---|---|
| Norte (0) | Derecha | $(0+1) \bmod 4 = 1$ = Este | `Este` | sí |
| Este (1) | Derecha | $(1+1) \bmod 4 = 2$ = Sur | `Sur` | sí |
| Sur (2) | Derecha | $(2+1) \bmod 4 = 3$ = Oeste | `Oeste` | sí |
| Oeste (3) | Derecha | $(3+1) \bmod 4 = 0$ = Norte | `Norte` | sí |
| Norte (0) | Izquierda | $(0-1) \bmod 4 = 3$ = Oeste | `Oeste` | sí |
| Oeste (3) | Izquierda | $(3-1) \bmod 4 = 2$ = Sur | `Sur` | sí |
| Sur (2) | Izquierda | $(2-1) \bmod 4 = 1$ = Este | `Este` | sí |
| Este (1) | Izquierda | $(1-1) \bmod 4 = 0$ = Norte | `Norte` | sí |

Como `Direccion` y `Sentido` son `sealed`, estas 8 parejas son todas las
posibles, así que el `match` es exhaustivo. $\blacksquare$

### Consecuencias que se usan después

De $g$ se obtienen dos propiedades que las pruebas comprueban:

```math
g(g(d, \text{Derecha}), \text{Izquierda}) = (\text{idx}(d) + 1 - 1) \bmod 4 = \text{idx}(d)
```

```math
4 \cdot \delta(s) \equiv 0 \pmod 4
```

Un giro y su contrario se cancelan, y cuatro giros al mismo lado devuelven la
dirección original.

## Punto 2: `ejecutar`

### Especificación

Cada dirección $d$ tiene un vector de avance $v(d) = (v_x, v_y)$:

```math
v(\text{Norte}) = (0, 1),\quad v(\text{Este}) = (1, 0),\quad v(\text{Sur}) = (0, -1),\quad v(\text{Oeste}) = (-1, 0)
```

Se define el significado $\mathcal{S}(o)$ de una orden $o$ como una función de
estados en estados, con una ecuación por cada alternativa de `Orden`:

```math
\mathcal{S}(\text{Avanzar}(n))(x, y, d) = (x + n \cdot v_x(d),\; y + n \cdot v_y(d),\; d)
```

```math
\mathcal{S}(\text{Girar}(s))(x, y, d) = (x,\; y,\; g(d, s))
```

```math
\mathcal{S}(\text{Repetir}(k, o')) = \begin{cases} \mathrm{id} & \text{si } k \leq 0 \\ \mathcal{S}(o')^{k} & \text{si } k \geq 1 \end{cases}
```

```math
\mathcal{S}(\text{Secuencia}(o_1, \ldots, o_m)) = \mathcal{S}(o_m) \circ \cdots \circ \mathcal{S}(o_1)
```

donde $f^{0} = \mathrm{id}$ y $f^{k} = f \circ f^{k-1}$, y la secuencia vacía ($m = 0$)
es la identidad.

### Teorema

Para toda orden $o$ y todo estado $e$:

```math
\text{ejecutar}(e, o) = \mathcal{S}(o)(e)
```

### Demostración

Por inducción estructural sobre $o$. Hay un caso base por cada orden básica y
un caso de inducción por cada orden compuesta.

**Caso base: $o = \text{Avanzar}(n)$.** Sea $e = (x, y, d)$. El programa hace
`match` sobre `e.mira`:

| $d$ | Programa devuelve | Con $v(d)$ la especificación da |
|---|---|---|
| Norte | $(x,\; y + n,\; d)$ | $(x + n \cdot 0,\; y + n \cdot 1,\; d)$ |
| Sur | $(x,\; y - n,\; d)$ | $(x + n \cdot 0,\; y + n \cdot (-1),\; d)$ |
| Este | $(x + n,\; y,\; d)$ | $(x + n \cdot 1,\; y + n \cdot 0,\; d)$ |
| Oeste | $(x - n,\; y,\; d)$ | $(x + n \cdot (-1),\; y + n \cdot 0,\; d)$ |

En los cuatro casos coinciden, y esto vale también para $n$ negativo o cero.

**Caso base: $o = \text{Girar}(s)$.** El programa devuelve
`e.copy(mira = girar(e.mira, s))`, es decir $(x, y, \text{girar}(d, s))$. Por el
teorema del punto 1, $\text{girar}(d, s) = g(d, s)$, que es lo que dice la
especificación.

**Caso de inducción: $o = \text{Secuencia}(o_1, \ldots, o_m)$.** La hipótesis de
inducción (HI) es que $\text{ejecutar}(e', o_i) = \mathcal{S}(o_i)(e')$ para
todo $e'$ y todo $i$, porque cada $o_i$ es una orden más pequeña que la
secuencia. Se demuestra por inducción sobre la longitud $m$ de la lista que

```math
\text{ordenes.foldLeft}(e)(\ldots) = \mathcal{S}(o_m)(\cdots \mathcal{S}(o_1)(e) \cdots)
```

- Si $m = 0$: `foldLeft` sobre la lista vacía devuelve $e$, y la composición
  vacía es la identidad.
- Si $m \geq 1$: por la definición de `foldLeft`,
  $\text{foldLeft}(e)(o_1 :: \text{resto}) = \text{foldLeft}(\text{ejecutar}(e, o_1))(\text{resto})$.
  Por la HI, $\text{ejecutar}(e, o_1) = \mathcal{S}(o_1)(e)$. Por la hipótesis
  sobre la longitud, `resto` (de longitud $m - 1$) aplicado a ese estado da
  $\mathcal{S}(o_m)(\cdots \mathcal{S}(o_2)(\mathcal{S}(o_1)(e)) \cdots)$.

Esto es $\mathcal{S}(o_m) \circ \cdots \circ \mathcal{S}(o_1)$ aplicada a $e$.

**Caso de inducción: $o = \text{Repetir}(k, o')$.** La HI es
$\text{ejecutar}(e', o') = \mathcal{S}(o')(e')$ para todo $e'$. La llamada
recursiva del programa es sobre `Repetir(k - 1, o')`, que no es una subparte de
la orden, así que se hace una segunda inducción sobre $k$.

- Si $k \leq 0$: el programa devuelve $e$, y $\mathcal{S}(o) = \mathrm{id}$.
- Si $k \geq 1$: el programa calcula
  $\text{ejecutar}(\text{ejecutar}(e, o'), \text{Repetir}(k-1, o'))$.
  Por la HI, el estado interior es $\mathcal{S}(o')(e)$. Por la hipótesis sobre
  $k - 1$, el llamado externo da $\mathcal{S}(o')^{k-1}(\mathcal{S}(o')(e))$,
  que es $\mathcal{S}(o')^{k}(e)$.

Con los cuatro casos queda demostrado que
$\forall o, e : \text{ejecutar}(e, o) = \mathcal{S}(o)(e)$. $\blacksquare$

### Terminación

En cada llamado recursivo la entrada se hace estrictamente menor: en
`Secuencia` y `Repetir` la orden contenida es una subparte, y en `Repetir` el
llamado sobre `Repetir(k - 1, o')` baja $k$ hasta llegar a $k \leq 0$, donde la
recursión se corta. Por eso `ejecutar` siempre termina.

### Corolario: una secuencia equivale a ejecutar sus partes

Del caso de `Secuencia` con $m = 2$ se obtiene, para todo $e$:

```math
\text{ejecutar}(e, \text{Secuencia}(o_1, o_2)) = \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)
```

Ejecutar una orden compuesta es ejecutar una parte y después la otra sobre el
estado que dejó la primera. Este hecho es el que se usa para justificar
`componer` en el punto 4 y las reglas de `simplificar` en el punto 5.

## Punto 3 — `cuantasBasicas` y `profundidad`

**Especificación de $b$.**

$$
b(\text{Avanzar}(n)) = 1, \qquad b(\text{Girar}(s)) = 1,
$$
$$
b(\text{Repetir}(k, o)) = \begin{cases} 0 & k \le 0 \\ k \cdot b(o) & k > 0 \end{cases},
\qquad
b(\text{Secuencia}([o_1,\dots,o_n])) = \sum_{i=1}^{n} b(o_i).
$$

**Lema auxiliar (`suma`).** Para toda lista $\ell = [o_1,\dots,o_n]$ y todo
$a \in \mathbb{Z}$, suponiendo que `cuantasBasicas` es correcta sobre cada
$o_i$:

$$
\text{suma}(\ell, a) = a + \sum_{i=1}^{n} b(o_i).
$$

*Demostración por inducción sobre $n$.* Si $n = 0$, `suma(Nil, a)` devuelve
`a`, que es $a + 0$. Si $\ell = h :: t$, `suma(h :: t, a)` llama a
`suma(t, a + cuantasBasicas(h))`. Por hipótesis de inducción sobre $t$ y por
la corrección de `cuantasBasicas(h)` $= b(h)$, esto vale
$a + b(h) + \sum_{o \in t} b(o) = a + \sum_{i=1}^{n} b(o_i)$. $\square$

**Teorema.** Para todo $o$, `cuantasBasicas(o)` $= b(o)$.

*Demostración por inducción estructural sobre $o$.*

* **Avanzar y Girar.** Devuelven 1, igual que $b$.
* **Repetir(k, o).** Si `veces <= 0` devuelve 0, igual que $b$. Si no,
  devuelve `veces * cuantasBasicas(orden)`, y por hipótesis de inducción
  `cuantasBasicas(orden)` $= b(\text{orden})$, luego da $k \cdot b(o)$.
* **Secuencia(ℓ).** Devuelve `suma(ordenes, 0)`. Por el lema, por hipótesis
  de inducción sobre cada elemento y con $a = 0$, vale $\sum_i b(o_i)$. $\square$
  **Especificación de $p$.**

$$
p(\text{Avanzar}(n)) = p(\text{Girar}(s)) = 1, \qquad
p(\text{Repetir}(k, o)) = 1 + p(o),
$$
$$
p(\text{Secuencia}(\ell)) = 1 + \max\bigl(\{0\} \cup \{p(o) : o \in \ell\}\bigr).
$$

El $0$ dentro del máximo solo importa para $\ell = []$: da
$p(\text{Secuencia}(\text{Nil})) = 1$, como pide el enunciado. Para $\ell$ no
vacía no cambia el máximo, porque toda $p(o) \ge 1$.

**Lema auxiliar (`maximo`).** Para toda lista $\ell$ y todo $m \in \mathbb{Z}$:

$$
\text{maximo}(\ell, m) = \max\bigl(\{m\} \cup \{p(o) : o \in \ell\}\bigr).
$$

*Demostración por inducción sobre la longitud de $\ell$.* Con `Nil` devuelve
`mejor`, o sea $m$. Con `h :: t`, llama a `maximo(t, m')` con
$m' = \max(m, p(h))$, porque `if (ph > mejor) ph else mejor` es exactamente
$\max$. Por hipótesis de inducción da $\max(\{m'\} \cup \{p(o) : o \in t\})$,
que es $\max(\{m\} \cup \{p(o) : o \in \ell\})$. $\square$

**Teorema.** Para todo $o$, `profundidad(o)` $= p(o)$.
*Demostración por inducción estructural.* Las básicas devuelven 1.
`Repetir(_, orden)` devuelve `1 + profundidad(orden)` y por hipótesis de
inducción es $1 + p(\text{orden})$. `Secuencia(ordenes)` devuelve
`1 + maximo(ordenes, 0)`, que por el lema y la hipótesis de inducción es
$1 + \max(\{0\} \cup \{p(o) : o \in \ell\}) = p(\text{Secuencia}(\ell))$. $\square$

## Punto 4 — `componer`, `rutina`, `aplicarA` y `transformar`

### `componer`: la compuesta equivale a ejecutar sus partes

$$
\forall e, o_1, o_2.\quad \text{ej}\bigl(e, \text{componer}(o_1,o_2)\bigr) = \text{ej}\bigl(\text{ej}(e,o_1),\, o_2\bigr).
$$

*Demostración.* `componer(o1, o2)` es `Secuencia(List(o1, o2))`. Por la
ecuación de `Secuencia` demostrada en el Punto 2, ejecutarla aplica los
elementos de izquierda a derecha, acumulando el estado:
$\text{ej}(\text{ej}(e,o_1),o_2)$. $\square$

### `rutina`: la rutina compuesta equivale a ejecutar sus partes

Sea $\sigma_{p,g} = \text{Secuencia}([\text{Avanzar}(p), \text{Girar}(g)])$.
Por definición, $\text{rutina}(p)(g)(k) = \text{Repetir}(k, \sigma_{p,g})$.

**Afirmación 1 (el paso).**
$\text{ej}(e, \sigma_{p,g}) = \text{ej}\bigl(\text{ej}(e, \text{Avanzar}(p)),\,\text{Girar}(g)\bigr)$,
por la ecuación de `Secuencia` (igual que en `componer`).

**Afirmación 2 (las repeticiones).** Para todo $k \ge 0$ se cumple
$\text{ej}(e, \text{rutina}(p)(g)(k)) = \text{ej}(e, \text{Secuencia}(\underbrace{[\sigma_{p,g},\dots,\sigma_{p,g}]}_{k}))$.

*Demostración por inducción sobre $k$.* Para $k = 0$, `Repetir(0, ·)` no
hace nada y `Secuencia(Nil)` tampoco: ambos devuelven $e$. Para $k+1$,
`repite` ejecuta una vez $\sigma_{p,g}$ y sigue con $k$ repeticiones; por
hipótesis de inducción eso es ejecutar $\sigma_{p,g}$ y luego la secuencia
de $k$ copias, es decir, la secuencia de $k+1$ copias. $\square$

En palabras: la rutina compuesta equivale a ejecutar, una tras otra, sus $k$
copias del paso, y cada paso equivale a avanzar y luego girar.

**Currificación.** `def rutina(pasos)(giro)(veces) = E` abrevia
`pasos => (giro => (veces => E))`; fijar `pasos` y `giro` produce una
función `Int => Orden` con la misma definición. Ejemplo: con $p = 2$,
$g = \text{Derecha}$ y $k = 3$ el robot queda en $(2,0,\text{Oeste})$
(ver la sección de `rutina` del informe de proceso).

### `aplicarA`

**Lema (`recorre`).** Para toda lista de estados $\ell$ y toda lista
$acum$:

$$
\text{recorre}(\ell, acum) = \text{reverse}(acum) \mathbin{+\!+} [\text{ej}(e,o) : e \in \ell].
$$

*Demostración por inducción sobre la longitud de $\ell$.* Con `Nil`
devuelve `acum.reverse`. Con `h :: t`, llama a
`recorre(t, ejecutar(h, o) :: acum)`; por hipótesis de inducción da
$\text{reverse}(\text{ej}(h,o) :: acum) \mathbin{+\!+} [\text{ej}(e,o) : e \in t]$ y
$\text{reverse}(x :: acum) = \text{reverse}(acum) \mathbin{+\!+} [x]$, de donde sale la
igualdad. $\square$

Con $acum = []$: $\text{aplicarA}(\ell, o) = [\text{ej}(e_1,o),\dots,\text{ej}(e_n,o)]$,
que es la especificación, en el mismo orden.

### `transformar`

Se define $T$ con las cuatro ecuaciones del enunciado. La demostración por
inducción estructural es la misma de siempre; en `Secuencia` se usa el lema
análogo al de `aplicarA`: $\text{recorre}(\ell, acum) =
\text{reverse}(acum) \mathbin{+\!+} [T(o,f) : o \in \ell]$.

## Limitaciones y casos en que el método se equivoca

1. **`profundidad` es estructural, no semántica.** `Repetir(0, Avanzar(1))`
   no hace nada, y `cuantasBasicas` lo sabe (devuelve 0), pero
   `profundidad` devuelve 2, porque cuenta niveles del árbol aunque nunca
   se ejecuten. Es lo que pide el enunciado, pero no debe leerse como "qué
   tan complicada es la ejecución".
2. **Desbordamiento de `Int` en `cuantasBasicas`.** Con
   `Repetir(Int.MaxValue, Repetir(2, Avanzar(1)))` el resultado correcto es
   $2(2^{31}-1)$, que no cabe en un `Int`: la multiplicación da vuelta y
   devuelve $-2$. El teorema vale para enteros matemáticos; sobre `Int` vale
   mientras $b(o) \le 2^{31} - 1$.
3. **`transformar` solo aplica `f` a las básicas.** Una `f` que quiera
   cambiar un `Repetir`, por ejemplo
   `{ case Repetir(_, o) => o; case otra => otra }`, no tendrá efecto:
   `transformar(Repetir(2, Avanzar(1)), f)` sigue siendo `Repetir(2, Avanzar(1))`.
4. **Profundidad de pila.** Las funciones de cola recorren listas largas sin
   problema, pero una orden anidada decenas de miles de niveles (no larga,
   sino *honda*) haría crecer la pila en `profundidad`, `cuantasBasicas` y
   `transformar`, porque la recursión sobre el árbol no es de cola.

## Punto 5 — `simplificar`

### Especificación

Dos órdenes son equivalentes, $o \equiv o'$, si dejan al robot en el mismo
estado final desde cualquier estado inicial. Por el teorema del Punto 2, esto
es lo mismo que pedir igualdad de significados:

$
o \equiv o' \iff \mathcal{S}(o) = \mathcal{S}(o')
$

`simplificar` reescribe una orden con cinco reglas, cada una una equivalencia:

$
\begin{aligned}
&\text{(R1)} && \text{Avanzar}(0) \equiv \text{Repetir}(k, o) \equiv \text{Secuencia}(\text{Nil}) && (k \le 0) \\
&\text{(R2)} && [\ldots, \text{Avanzar}(a), \text{Avanzar}(b), \ldots] \equiv [\ldots, \text{Avanzar}(a+b), \ldots] && \text{(dentro de una } \text{Secuencia}) \\
&\text{(R3)} && \text{Repetir}(1, o) \equiv o \\
&\text{(R4)} && \text{Secuencia}([o]) \equiv o \\
&\text{(R5)} && [\ldots, \text{Secuencia}(\text{Nil}), \ldots] \equiv [\ldots] && \text{(dentro de una } \text{Secuencia})
\end{aligned}
$

La implementación tiene dos funciones auxiliares:

* `paso(o)` aplica las reglas una vez, de arriba hacia abajo, también sobre los hijos.
* `limpiar(l)` recorre la lista de una `Secuencia` aplicando R2 y R5 (usa un acumulador `acum` en orden inverso, como `aplicarA`).

`simplificar(o)` repite `paso` hasta que `paso(o) == o` (punto fijo).

### Teorema

Para toda orden $o$ y todo estado $e$:

$
\text{ejecutar}(e, \text{simplificar}(o)) = \text{ejecutar}(e, o)
$

### Notación y lemas previos

Para una lista de órdenes $\ell = [o_1, \ldots, o_m]$ se escribe

$
\mathcal{S}^*(\ell) = \mathcal{S}(o_m) \circ \cdots \circ \mathcal{S}(o_1)
$

de modo que $\mathcal{S}(\text{Secuencia}(\ell)) = \mathcal{S}^*(\ell)$ por la definición del Punto 2.

**Lema 1 (composición).** Para listas $\ell_1, \ell_2$:
$\mathcal{S}^*(\ell_1 \mathbin{+\!+} \ell_2) = \mathcal{S}^*(\ell_2) \circ \mathcal{S}^*(\ell_1)$.

*Demostración.* Es asociatividad de la composición: los elementos de $\ell_1$
se aplican primero y los de $\ell_2$ después. $\square$

**Lema 2 (congruencia).** Si $\mathcal{S}(o) = \mathcal{S}(o')$, entonces
sustituir $o$ por $o'$ dentro de una `Repetir` o de una `Secuencia` no cambia el
significado.

*Demostración.* $\mathcal{S}(\text{Repetir}(k, o)) = \mathcal{S}(o)^{k}$ solo
depende de $\mathcal{S}(o)$, y $\mathcal{S}^*(\ell)$ es una composición de los
$\mathcal{S}(o_i)$. $\square$

**Lema 3 (las cinco reglas).** Cada regla preserva $\mathcal{S}$.

* **R1.** $\mathcal{S}(\text{Avanzar}(0))(x, y, d) = (x + 0 \cdot v_x(d),\; y + 0 \cdot v_y(d),\; d) = (x, y, d)$, así que es $\mathrm{id}$.
  $\mathcal{S}(\text{Repetir}(k, o)) = \mathrm{id}$ si $k \le 0$ por la especificación del Punto 2.
  $\mathcal{S}(\text{Secuencia}(\text{Nil})) = \mathrm{id}$, porque la composición vacía es la identidad.
* **R2.** `Avanzar` no cambia la dirección, así que ambos avances usan el mismo $d$:

$
\begin{aligned}
\bigl(\mathcal{S}(\text{Avanzar}(b)) \circ \mathcal{S}(\text{Avanzar}(a))\bigr)(x, y, d)
&= \bigl(x + a\,v_x(d) + b\,v_x(d),\; y + a\,v_y(d) + b\,v_y(d),\; d\bigr) \\
&= \bigl(x + (a+b)\,v_x(d),\; y + (a+b)\,v_y(d),\; d\bigr) \\
&= \mathcal{S}(\text{Avanzar}(a+b))(x, y, d)
\end{aligned}
$
Por el Lema 1, la igualdad se mantiene con cualquier prefijo y sufijo en la lista.
* **R3.** $\mathcal{S}(\text{Repetir}(1, o)) = \mathcal{S}(o)^1 = \mathcal{S}(o)$.
* **R4.** $\mathcal{S}(\text{Secuencia}([o])) = \mathcal{S}(o)$, porque la composición de un solo elemento es ese elemento.
* **R5.** Por R1, $\mathcal{S}(\text{Secuencia}(\text{Nil})) = \mathrm{id}$, y por el Lema 1
  $\mathcal{S}^*(\ell_1 \mathbin{+\!+} [\text{Secuencia}(\text{Nil})] \mathbin{+\!+} \ell_2) = \mathcal{S}^*(\ell_2) \circ \mathrm{id} \circ \mathcal{S}^*(\ell_1) = \mathcal{S}^*(\ell_1 \mathbin{+\!+} \ell_2)$. $\square$

**Lema 4 (`recorre`, dentro de `limpiar`).** Para toda lista `pendientes` $= p$ y toda lista `acum`:

$
\mathcal{S}^*\bigl(\text{recorre}(p, \text{acum})\bigr) = \mathcal{S}^*\bigl(\text{reverse}(\text{acum}) \mathbin{+\!+} p\bigr)
$

*Demostración por inducción sobre la longitud de $p$.*

* **$p = \text{Nil}$.** Devuelve `acum.reverse`, que es $\text{reverse}(\text{acum}) \mathbin{+\!+} \text{Nil}$.
* **$p = \text{Secuencia}(\text{Nil}) :: t$.** Llama a `recorre(t, acum)`. Por hipótesis de inducción (HI) da $\mathcal{S}^*(\text{reverse}(\text{acum}) \mathbin{+\!+} t)$, que por R5 es igual a $\mathcal{S}^*(\text{reverse}(\text{acum}) \mathbin{+\!+} \text{Secuencia}(\text{Nil}) :: t)$.
* **$p = \text{Avanzar}(b) :: t$ y $\text{acum} = \text{Avanzar}(a) :: r$.** Llama a `recorre(t, Avanzar(a+b) :: r)`. Por HI da $\mathcal{S}^*(\text{reverse}(r) \mathbin{+\!+} [\text{Avanzar}(a+b)] \mathbin{+\!+} t)$. Como $\text{reverse}(\text{acum}) = \text{reverse}(r) \mathbin{+\!+} [\text{Avanzar}(a)]$, por R2 esto es $\mathcal{S}^*(\text{reverse}(\text{acum}) \mathbin{+\!+} p)$.
* **Otros casos** (`Avanzar(b)` con `acum` vacío o con otra cabeza, o cualquier otra orden $h$). Se pasa $h$ a `acum` sin modificarla, y $\text{reverse}(h :: \text{acum}) \mathbin{+\!+} t = \text{reverse}(\text{acum}) \mathbin{+\!+} h :: t$. $\square$

Con $\text{acum} = \text{Nil}$: $\mathcal{S}^*(\text{limpiar}(\ell)) = \mathcal{S}^*(\ell)$.

### Demostración

**Paso 1: `paso` preserva el significado.** Se demuestra
$\mathcal{S}(\text{paso}(o)) = \mathcal{S}(o)$ por inducción estructural sobre $o$.

* **$o = \text{Avanzar}(0)$ o $o = \text{Repetir}(k, \_)$ con $k \le 0$.** Devuelve `Secuencia(Nil)`, y por R1 ambos son $\mathrm{id}$.
* **$o = \text{Repetir}(1, o')$.** Devuelve `paso(o')`. Por HI, $\mathcal{S}(\text{paso}(o')) = \mathcal{S}(o')$, y por R3 esto es $\mathcal{S}(o)$.
* **$o = \text{Repetir}(k, o')$ con $k \ge 2$.** Devuelve `Repetir(k, paso(o'))`. Por HI y el Lema 2, tiene el mismo significado que $o$.
* **$o = \text{Secuencia}([u])$.** Devuelve `paso(u)`. Por HI y R4, $\mathcal{S}(\text{paso}(u)) = \mathcal{S}(u) = \mathcal{S}(o)$.
* **$o = \text{Secuencia}(\ell)$ en cualquier otro caso.** Devuelve `Secuencia(limpiar(ℓ.map(paso)))`. Por HI cada $\text{paso}(o_i)$ tiene el mismo significado que $o_i$, y por el Lema 2 $\mathcal{S}^*(\ell.\text{map}(\text{paso})) = \mathcal{S}^*(\ell)$. Por el Lema 4, `limpiar` tampoco lo cambia.
* **Otra orden básica** (`Girar` o `Avanzar(n)` con $n \ne 0$). Devuelve $o$.

**Paso 2: `simplificar` preserva el significado.** `simplificar(o)` es `paso` aplicado un número finito $r$ de veces (finito por la sección siguiente). Por el Paso 1 y por inducción sobre $r$:

$
\mathcal{S}(\text{simplificar}(o)) = \mathcal{S}(o)
$

**Paso 3: conclusión.** Por el teorema del Punto 2, `ejecutar` coincide con $\mathcal{S}$:

$
\text{ejecutar}(e, \text{simplificar}(o)) = \mathcal{S}(\text{simplificar}(o))(e) = \mathcal{S}(o)(e) = \text{ejecutar}(e, o) \quad \blacksquare
$

### Terminación

Sea $N(o)$ el número de nodos del árbol de $o$ y $Z(o)$ el número de nodos
$\text{Avanzar}(0)$ que contiene. Se toma la medida $\mu(o) = (N(o), Z(o))$
con orden lexicográfico. Si $\text{paso}(o) \ne o$, alguna regla se aplicó, y:

* R1 sobre $\text{Repetir}(k, o')$, R2, R3, R4 y R5 reducen $N$ (ninguna lo aumenta).
* R1 sobre $\text{Avanzar}(0)$ deja $N$ igual y reduce $Z$ en uno.
* R2 puede crear un $\text{Avanzar}(0)$ (por ejemplo, $a = 1$, $b = -1$), pero lo hace reduciendo $N$, y el orden lexicográfico prioriza $N$.

Así $\mu$ decrece estrictamente en cada iteración que cambia algo. Como $\mu$ toma valores en
$\mathbb{N} \times \mathbb{N}$ con orden bien fundado, no hay cadenas infinitas, y
`simplificar` llega a un punto fijo en un número finito de iteraciones.

### Limitaciones

1. **No es una forma normal completa.** Solo aplica las cinco reglas. `Repetir(2, Avanzar(1))` es equivalente a `Avanzar(2)`, y `Secuencia(List(Girar(Derecha), Girar(Izquierda)))` a `Secuencia(Nil)`, pero `simplificar` no los reconoce.
2. **Desbordamiento de `Int` en la fusión.** La regla R2 calcula `a + b`; con valores cercanos a `Int.MaxValue` el resultado da la vuelta. El teorema vale para enteros matemáticos.
3. **Profundidad de pila.** `paso` recorre el árbol con recursión que no es de cola; una orden muy *honda* (decenas de miles de niveles) puede desbordar la pila. Las listas largas sí se recorren sin problema con `limpiar`.