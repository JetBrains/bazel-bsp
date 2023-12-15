
#[cfg(feature = "foo")]
use itoa::Buffer;

pub fn foo() {
    let mut buffer = Buffer::new();
    let printed = buffer.format(128u64);
    assert_eq!(printed, "128");
}
