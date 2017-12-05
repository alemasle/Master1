; Target
target triple = "x86_64-unknown-linux-gnu"
; External declaration of the printf function
declare i32 @printf(i8* noalias nocapture, ...)

; Actual code begins



define i32 @main(){
  %x = alloca i32
  %tmp1 = add i32 1, 2
  store i32 %tmp1, i32* %x
  %tmp2 = load i32, i32* %x
  ret i32 %tmp2
}
