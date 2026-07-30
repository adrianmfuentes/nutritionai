// src/services/notification.service.ts
import { logger } from '../utils/logger';
import { DeviceTokenModel } from '../models/DeviceToken';

let app: import('firebase-admin').app.App | null = null;
let initAttempted = false;

function getFirebaseApp(): import('firebase-admin').app.App | null {
  if (initAttempted) return app;
  initAttempted = true;

  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64;
  if (!raw) {
    logger.warn('FIREBASE_SERVICE_ACCOUNT_BASE64 no configurado: las notificaciones push están deshabilitadas.');
    return null;
  }

  try {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const admin = require('firebase-admin');
    const serviceAccount = JSON.parse(Buffer.from(raw, 'base64').toString('utf8'));
    app = admin.apps.length
      ? admin.app()
      : admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
    logger.info('Firebase Admin inicializado para notificaciones push.');
    return app;
  } catch (error) {
    logger.error('Error inicializando Firebase Admin:', error);
    return null;
  }
}

export class NotificationService {
  isEnabled(): boolean {
    return getFirebaseApp() !== null;
  }

  async sendToUser(userId: string, notification: { title: string; body: string; data?: Record<string, string> }): Promise<void> {
    const firebaseApp = getFirebaseApp();
    if (!firebaseApp) return;

    const tokens = await DeviceTokenModel.findByUser(userId);
    if (tokens.length === 0) return;

    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const admin = require('firebase-admin');
    try {
      const response = await admin.messaging(firebaseApp).sendEachForMulticast({
        tokens: tokens.map((t) => t.fcm_token),
        notification: { title: notification.title, body: notification.body },
        data: notification.data || {},
      });

      const invalidTokens: string[] = [];
      response.responses.forEach((r: any, i: number) => {
        if (!r.success && (r.error?.code === 'messaging/registration-token-not-registered' || r.error?.code === 'messaging/invalid-registration-token')) {
          invalidTokens.push(tokens[i].fcm_token);
        }
      });
      if (invalidTokens.length > 0) {
        await DeviceTokenModel.deleteTokens(invalidTokens);
      }
    } catch (error) {
      logger.error(`Error enviando push a usuario ${userId}:`, error);
    }
  }
}
