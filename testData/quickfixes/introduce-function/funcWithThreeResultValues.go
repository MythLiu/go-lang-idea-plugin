package main

func main() {
  var (
    i int
    r struct {i int}
    q string
   )

  i, r, q = asd<caret>()
}