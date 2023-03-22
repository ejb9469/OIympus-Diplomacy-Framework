import requests

r = requests.get("http://web.inter.nl.net/users/L.B.Kruijswijk/#6.A")
text = r.text

text = text.split("6. TEST CASES</h3></a>")[1]
text = text.split("7. COLONIAL VARIANT</h3></a>")[0]

#print(text)

raw_arr = text.split("<p><pre>")
del raw_arr[0]

testcase_str_arr = []

for block in raw_arr:
    testcase_str_arr.append(block.split("</pre>")[0])

removals = []
for i in range(len(testcase_str_arr)):
    if "Build" in testcase_str_arr[i] or "Remove" in testcase_str_arr[i] or "build" in testcase_str_arr[i] or "remove" in testcase_str_arr[i]:
        #testcase_str_arr.pop(i)
        removals.append(testcase_str_arr[i])

for removal in removals:
    for i in range(len(testcase_str_arr)):
        if testcase_str_arr[i] == removal:
            del testcase_str_arr[i]
            break

i = 1
for testcase_str in testcase_str_arr:
    if i >= 20 and i <= 40:
        pass
        #print(testcase_str)
        #print("==")
    i += 1

order_sheets = []
for testcase_str in testcase_str_arr:
    
    country = "NONE"
    order_sheets.append([])
    for line in testcase_str.split("\n"):

        order = ""
        
        if line.isspace() or not line:
            continue
        
        if ":" in line:  # Country header - e.g. "France:", "Italy:"
            country = line.split(":")[0].upper()
            continue
        order += country + " "
        
        # e.g. F Tyrrhenian Sea Convoys A Tunis - Naples
        
        order_type = " -"
        if "Supports" in line:
            order_type = " Supports"
        elif "Convoys" in line:
            order_type = " Convoys"
        elif "Hold" in line:
            order_type = " Hold"

        unit = line.split(order_type)[0]
        order += unit
        order += order_type[0:2]

        remainder = line.split(order_type)[1]
        remainder = remainder.replace("(", ".").replace(")", "")

        if len(remainder) >= 2:
            if remainder[1] == "A":
                remainder = remainder.replace("A ", "", 1)
            elif remainder[1] == "F":
                remainder = remainder.replace("F ", "", 1)

        if order_type == " Supports" and not "-" in remainder:
            remainder += " H"

        remainder = remainder.replace("via Convoy", "VIA CONVOY")
        
        order += remainder
        order_sheets[len(order_sheets)-1].append(order)

i = 1
for testcase_arr in order_sheets:
    filename = "_pythongen_testcase_" + str(i) + ".txt"
    file = open(filename, "w")
    prev_country = "NONE"
    for order_str in testcase_arr:
        newline = ""
        if prev_country == "NONE":
            prev_country = order_str.split(" ")[0]
        elif prev_country != order_str.split(" ")[0]:
            prev_country = order_str.split(" ")[0]
            newline = "\n"
        file.write(newline + order_str + "\n")
    file.close()
    i += 1
