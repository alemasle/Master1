; Target
target triple = "x86_64-unknown-linux-gnu"
; External declaration of the printf function
declare i32 @printf(i8* noalias nocapture, ...)

; Actual code begins



define i32 @main() {
%tmp1 = add i32 2, 2
%tmp2 = add i32 2, %tmp1
%tmp3 = add i32 2, %tmp2
%tmp4 = add i32 2, %tmp3
%tmp5 = add i32 2, %tmp4
ret i32 %tmp5
}

