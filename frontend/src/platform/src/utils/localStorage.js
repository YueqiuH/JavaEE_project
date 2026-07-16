//保存缓存
export const setStorage = (name, data) => {
    localStorage.setItem(name, data);
}

//获取缓存
export const getStorage = (name) => {
    return localStorage.getItem(name);
}

//删除缓存
export const delStorage = (name) => {
    return localStorage.removeItem(name)
}
