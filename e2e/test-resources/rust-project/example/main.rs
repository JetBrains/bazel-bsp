
#[cfg(feature = "foo")]
mod sub;

use itertools::Itertools;

fn main() {
    for i in (1..10).interleave(vec![1, 2, 3]) {
        println!("{}", i);
    }

    #[cfg(feature = "foo")]
    sub::foo();

    example_lib::execute();
    println!("The answer is {}", example_lib::answer());
}
