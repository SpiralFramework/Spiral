import * as utf8 from "utf8";

export function isBlank(str: string): boolean {
    return str.length == 0 || matches(str, "^[\\s\\xA0]+$")
}

export function matches(str: String, regex: string | RegExp): boolean {
    const result = str.match(regex);
    return result != null && result.length != 0;
}

export function toIntVariable(str: string): number {
    if (str.startsWith("0b")) return parseInt(str.substr(2), 2);
    else if (str.startsWith("0o")) return parseInt(str.substr(2), 8);
    else if (str.startsWith("0x")) return parseInt(str.substr(2), 16);
    else if (str.startsWith("0d")) return parseInt(str.substr(2), 10);
    else return parseInt(str);
}

export function pushString(array: number[], string: string) {
    const encoded = utf8.encode(string);
    let counter = 0;
    const length = encoded.length;
    while (counter < length) {
        array.push(encoded.charCodeAt(counter++));
    }
}

export function pushVariableInt16(array: number[], number: number) {
    pushVariableInt(array, number, 2);
}

export function pushVariableInt(array: number[], number: number, bytes: number) {
    let num = number;
    if (num < 0x80) {
        array.push(num);
        return;
    } else {
        array.push(num & 0xFF | 0x80);
        num >>= 7;
        for (let i = 0; i < bytes - 1; i++) {
            if (num < 0x80) {
                array.push(num);
                return;
            } else {
                array.push(num & 0xFF | 0x80);
                num >>= 8;
            }
        }
    }
}