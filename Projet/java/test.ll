; Target
target triple = "x86_64-unknown-linux-gnu"
; External declaration of the printf function
declare i32 @printf(i8* noalias nocapture, ...)

; Actual code begins



define i32 @main() {
%tmp1 = add i32 2, 4
%tmp2 = add i32 2, %tmp1
%tmp3 = sub i32 2, %tmp2
%tmp4 = add i32 8, %tmp3
ret i32 %tmp4
}

