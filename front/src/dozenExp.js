/**
 * Created by user on 2017/1/28.
 */
export function toDozenStr(v) {
    if (v == null)
        return ""
    else {
        let ret = parseInt(v / 12).toString()
        let fraction = v % 12
        if (fraction != 0) {
            if (v % 12 < 10)
                ret += '.0' + fraction
            else
                ret += '.' + fraction
        }

        return ret
    }
}

export function fromDozenStr(v) {
    if (v == null || v == '')
        return null

    let vStr = "" + v
    let num = vStr.split('.', 2)

    let ret = parseInt(num[0], 10) * 12
    if (num.length == 2){
        if(num[1] != '')
            ret += parseInt(num[1]) % 12
    }


    return ret
}
