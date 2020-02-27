class AuthorizeApiRequest
    prepend SimpleCommand
  
    def initialize(headers = {})
        @headers = headers
    end
  
    def call
        user if account_activated(http_auth_header)
    end

    public 

    def account_activated(token) 
        if check_token(token)
            user_found = User.find(decoded_auth_token[:user_id])
            if user_found.email_confirmed
                return true
            else
                errors.add(:token, 'Account not activated')
                return false
            end
        end
    end
    
    def check_token(token)
        @decoded_token = decoded_auth_token(token)
        if @decoded_token[:exp] < Time.now.to_i
            errors.add(:token, 'Token expired, please login again')
            return false
        elsif BlacklistedToken.exists?(token: http_auth_header)
            errors.add(:token, 'Token in blacklist, please login again')
            return false
        else
            return true
        end
    end
  
    private
  
    attr_reader :headers
  
    def user
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
        @user || errors.add(:token, 'Invalid token') && nil
    end
  
    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken.decode(http_auth_header)
    end
  
    def http_auth_header
        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end