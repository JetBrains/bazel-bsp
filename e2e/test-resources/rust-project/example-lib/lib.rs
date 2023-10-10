use std::time::Instant;

pub fn answer() -> u32 {
    42
}

pub fn execute() {
    let start = Instant::now();
    
    // Program
    
    let duration = start.elapsed();
    println!("Execution time: {:?}", duration);
}