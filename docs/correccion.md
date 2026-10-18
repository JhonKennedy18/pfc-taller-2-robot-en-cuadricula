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
 