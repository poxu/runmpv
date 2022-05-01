set objShell = WScript.CreateObject("WScript.Shell")
objShell.AppActivate WScript.Arguments.Item(0)
' restore (unminimize) window'
objShell.SendKeys "% {ENTER}"

' https://stackoverflow.com/a/56122113
' Dim oShell : Set oShell = CreateObject("WScript.Shell")
' oShell.SendKeys("% {DOWN}{DOWN}{DOWN}{DOWN}{ENTER}")   'Maximize
'...
' oShell.SendKeys("% {ENTER}")   'Restore
'...
' oShell.SendKeys("% {DOWN}{DOWN}{DOWN}{ENTER}")   'Minimize
