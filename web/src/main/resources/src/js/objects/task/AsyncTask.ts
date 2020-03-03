import {TaskCallback} from "@/objects/task/TaskCallback";
import {NetworkCallStatus} from "@/objects/network/NetworkCallStatus";

export interface AsyncTask {
    readonly callback: TaskCallback;

    apiKey: string;
    apiUrl: string;

    provideApiDetails(apiKey: string, apiUrl: string): void;

    execute(): void;

    onComplete(status: NetworkCallStatus): void;
}