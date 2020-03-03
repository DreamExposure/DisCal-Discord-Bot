import {TaskType} from "@/enums/TaskType";

export class NetworkCallStatus {
    private readonly _success: boolean;
    private readonly _type: TaskType;
    private _code: number;
    private _message: string;

    private _body: any;

    constructor(success: boolean, type: TaskType) {
        this._success = success;
        this._type = type;
    }

    //Getter/setter pairs
    get isSuccess() {
        return this._success;
    }

    get type() {
        return this._type;
    }

    get code() {
        return this._code;
    }

    set code(code) {
        this._code = code;
    }

    get message() {
        return this._message;
    }

    set message(message) {
        this._message = message;
    }

    get body() {
        return this._body;
    }

    set body(body) {
        this._body = body;
    }
}