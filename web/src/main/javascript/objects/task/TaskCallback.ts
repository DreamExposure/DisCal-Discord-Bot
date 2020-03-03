import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";

export interface TaskCallback {
    onCallback(status: NetworkCallStatus): void;
}