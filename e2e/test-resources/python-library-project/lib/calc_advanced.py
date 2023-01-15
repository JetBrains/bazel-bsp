def power(x, y):
    temp = 0
    if y == 0:
        return 1
    temp = power(x, y // 2)
    if y % 2 == 0:
        return temp * temp
    else:
        return x * temp * temp


def floor_sqrt(x):
    if x == 0 or x == 1:
        return x

    i = 1
    result = 1
    while result <= x:
        i += 1
        result = i * i

    return i - 1
