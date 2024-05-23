package lib

import "testing"

func TestSayHello(t *testing.T) {
    got := SayHello()
    want := "hello world"
    if got != want {
        t.Errorf("got %q want %q", got, want)
    }
}
