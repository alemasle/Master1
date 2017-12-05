; Target
target triple = "x86_64-unknown-linux-gnu"
; External declaration of the printf function
declare i32 @printf(i8* noalias nocapture, ...)

; Actual code begins



define void @main(){
  %x = alloca i32
  %y = alloca i32
  store i32 1, i32* %x
  %tmp1 = load i32, i32* %x
  %tmp2 = icmp ne i32 %tmp1, 0
  br i1 %tmp2, label %then1, label %else3

then1:
  store i32 1, i32* %y
  br label %fi2

else3:
  store i32 2, i32* %y
  br label %fi2

fi2:
  ret void
}
